package com.irateam.vkplayer.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.irateam.vkplayer.database.AudioDatabaseHelper;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.models.Settings;
import com.irateam.vkplayer.notifications.DownloadNotification;
import com.irateam.vkplayer.receivers.DownloadFinishedReceiver;
import com.irateam.vkplayer.utils.AudioUtils;
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
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DownloadService extends Service {

    public static final String AUDIO_LIST = "audio_list";
    public static final String DOWNLOAD_FINISHED = "download_service.download_finished";
    public static final String DOWNLOAD_ONE = "download_service.download_one";

    public static final String START_SYNC = "start_sync";
    public static final String STOP_DOWNLOADING = "stop_downloading";
    public static final String START_DOWNLOADING = "start_downloading";

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
                    sync();
                    break;
            }
        }
        return START_STICKY;
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
                        if (audio.id == iterator.next().id) {
                            iterator.remove();
                            break;
                        }
                    }
                }

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
            }
        });
    }

    public void download() {
        currentThread = new Thread(() -> {
            Audio audio;
            do {
                audio = syncQueue.poll();
                boolean isSync = true;
                if (audio == null) {
                    audio = downloadQueue.poll();
                    isSync = false;
                }
                if (audio != null) {
                    startForeground(DownloadNotification.ID, DownloadNotification.create(this, audio, 0, isSync));
                    BufferedInputStream inputStream = null;
                    FileOutputStream fileOutputStream = null;
                    URLConnection connection = null;
                    File file = new File(getExternalCacheDir(), String.valueOf(audio.id));
                    try {
                        connection = new URL(audio.url).openConnection();
                        int size = connection.getContentLength();
                        inputStream = new BufferedInputStream(connection.getInputStream());
                        fileOutputStream = new FileOutputStream(file);
                        final byte data[] = new byte[1024];
                        int count, total = 0, progress = 0, now;
                        while ((count = inputStream.read(data, 0, 1024)) != -1) {

                            if (Thread.interrupted()) {
                                stopForeground(true);
                                return;
                            }

                            total += count;
                            fileOutputStream.write(data, 0, count);
                            now = (int) ((double) total / size * 100);
                            if (now - progress > 3) {
                                progress = now;
                                DownloadNotification.update(this, audio, progress, isSync);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    audio.cachePath = file.getAbsolutePath();
                    databaseHelper.cache(audio);

                    Intent intent = new Intent(DOWNLOAD_FINISHED);
                    intent.putExtra(DownloadFinishedReceiver.AUDIO_ID, audio);
                    sendBroadcast(intent);
                }
            } while (audio != null);
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
