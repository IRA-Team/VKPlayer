/*
 * Copyright (C) 2015 IRA-Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irateam.vkplayer.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.database.AudioDatabaseHelper;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.models.Settings;
import com.irateam.vkplayer.notifications.DownloadNotification;
import com.irateam.vkplayer.receivers.DownloadFinishedReceiver;
import com.irateam.vkplayer.utils.AudioUtils;
import com.irateam.vkplayer.utils.NetworkUtils;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DownloadService extends Service {

    public static final String AUDIO_LIST = "audio_list";
    public static final String DOWNLOAD_FINISHED = "download_service.download_finished";

    public static final String START_SYNC = "start_sync";
    public static final String STOP_DOWNLOADING = "stop_downloading";
    public static final String START_DOWNLOADING = "start_downloading";
    public static final String USER_SYNC = "user_sync";

    private Thread currentThread;
    private Queue<Audio> downloadQueue = new ConcurrentLinkedQueue<>();
    private Queue<Audio> syncQueue = new ConcurrentLinkedQueue<>();

    private AudioDatabaseHelper databaseHelper;
    private Settings settings;

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new AudioDatabaseHelper(this);
        settings = Settings.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case START_DOWNLOADING:
                    ArrayList<Audio> list = (ArrayList<Audio>) intent.getSerializableExtra(AUDIO_LIST);
                    for (Audio audio : list) {
                        downloadQueue.add(audio);
                    }
                    if (!isDownloading()) {
                        download();
                    }
                    break;


                case STOP_DOWNLOADING:
                    stopDownloading();
                    break;

                case START_SYNC:
                    if (intent.getExtras().getBoolean(USER_SYNC, false)) {
                        if (NetworkUtils.checkNetwork(this)) {
                            sync();
                        } else {
                            Toast.makeText(this, R.string.error_no_internet_connection, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if (settings.isWifiSync()) {
                            if (NetworkUtils.checkWifiNetwork(this)) {
                                sync();
                            } else {
                                DownloadNotification.errorSync(this, getString(R.string.error_no_wifi_connection));
                            }
                        } else {
                            sync();
                        }
                    }
                    break;
            }
        }
        return START_NOT_STICKY;
    }


    private void sync() {
        VKApi.audio().get(VKParameters.from(VKApiConst.COUNT, settings.getSyncCount())).executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                List<Audio> vkList = AudioUtils.parseJSONResponseToList(response);
                List<Audio> cachedList = new AudioDatabaseHelper(DownloadService.this).getAll();

                for (Audio audio : cachedList) {
                    for (Iterator<Audio> iterator = vkList.iterator(); iterator.hasNext(); ) {
                        if (iterator.next().equalsId(audio) && audio.isCached()) {
                            iterator.remove();
                            break;
                        }
                    }
                }

                Collections.reverse(vkList);
                syncQueue = new ConcurrentLinkedQueue<>();
                for (Audio audio : vkList) {
                    syncQueue.add(audio);
                }

                if (!isDownloading()) {
                    download();
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                DownloadNotification.errorSync(DownloadService.this, error.errorMessage);
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                DownloadNotification.error(DownloadService.this, true);
            }
        });
    }

    //TODO: Refactor this hell!
    public void download() {
        currentThread = new Thread(() -> {
            Audio audio;
            boolean syncFlag;
            int audioLeftCount;
            int audioTotalCount = 0;
            do {
                audioLeftCount = syncQueue.size();
                if (audioLeftCount > 0) {
                    audio = syncQueue.poll();
                    syncFlag = true;
                } else {
                    audioLeftCount = downloadQueue.size();
                    audio = downloadQueue.poll();
                    syncFlag = false;
                }

                if (audio != null) {
                    startForeground(DownloadNotification.ID, DownloadNotification.create(this, audio, 0, audioLeftCount - 1, syncFlag));
                    File file = new File(settings.getAudioCacheDir(this), String.valueOf(audio.getId()));

                    URLConnection connection;
                    BufferedInputStream inputStream;
                    try {
                        connection = new URL(audio.getUrl()).openConnection();
                        inputStream = new BufferedInputStream(connection.getInputStream());
                    } catch (IOException e) {
                        Log.e("HAHAHA", "LOL");
                        continue;
                    }


                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        final byte buffer[] = new byte[1024];
                        int size = connection.getContentLength();
                        int currentBytes, currentProgress;
                        int totalBytes = 0, totalProgress = 0;

                        while ((currentBytes = inputStream.read(buffer, 0, 1024)) != -1) {
                            if (Thread.interrupted()) {
                                syncQueue = new ConcurrentLinkedQueue<>();
                                stopForeground(true);
                                return;
                            }

                            fileOutputStream.write(buffer, 0, currentBytes);
                            totalBytes += currentBytes;
                            currentProgress = (int) ((double) totalBytes / size * 100);

                            if (currentProgress - totalProgress > 3) {
                                totalProgress = currentProgress;
                                DownloadNotification.update(this, audio, totalProgress, audioLeftCount - 1, syncFlag);
                            }
                        }
                    } catch (IOException e) {
                        syncQueue = new ConcurrentLinkedQueue<>();
                        DownloadNotification.error(this, syncFlag);
                        stopForeground(true);
                        e.printStackTrace();
                        return;
                    }

                    audio.setCacheFile(file);
                    databaseHelper.cache(audio);

                    Intent intent = new Intent(DOWNLOAD_FINISHED);
                    intent.putExtra(DownloadFinishedReceiver.AUDIO_ID, audio);
                    sendBroadcast(intent);

                    audioTotalCount++;
                    if ((syncFlag && syncQueue.size() == 0) || (!syncFlag && downloadQueue.size() == 0)) {
                        DownloadNotification.successful(this, audioTotalCount, syncFlag);
                        audioTotalCount = 0;
                    }
                }
            } while (audio != null);
            stopForeground(true);
        });
        currentThread.start();
    }

    public void stopDownloading() {
        if (currentThread != null && !currentThread.isInterrupted()) {
            currentThread.interrupt();
        }
    }

    public boolean isDownloading() {
        return currentThread != null && currentThread.isAlive();
    }

}
