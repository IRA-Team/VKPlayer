package com.irateam.vkplayer.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.irateam.vkplayer.notifications.DownloadNotification;
import com.vk.sdk.api.model.VKApiAudio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class DownloadService extends Service {

    public static final String AUDIO_SET = "audio_set";

    private Thread currentThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("DOWNLOAD", "STARTED");
        ArrayList<VKApiAudio> list = (ArrayList<VKApiAudio>) intent.getSerializableExtra(AUDIO_SET);
        Log.i("DOWNLOAD", "SIZE: " + list.size());
        startDownload(list.get(0));
        return START_NOT_STICKY;
    }

    private void startDownload(final VKApiAudio audio) {
        startForeground(DownloadNotification.ID, DownloadNotification.create(this, audio));
        currentThread = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedInputStream inputStream = null;
                FileOutputStream fileOutputStream = null;
                URLConnection connection = null;
                try {
                    connection = new URL(audio.url).openConnection();
                    int size = connection.getContentLength();
                    inputStream = new BufferedInputStream(connection.getInputStream());
                    fileOutputStream = new FileOutputStream(new File(getExternalCacheDir(), String.valueOf(audio.id)));
                    final byte data[] = new byte[1024];
                    int count, total = 0;
                    while ((count = inputStream.read(data, 0, 1024)) != -1) {
                        total += count;
                        fileOutputStream.write(data, 0, count);
                        System.out.println((float) total / size * 100);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stopForeground(true);
            }
        });
        currentThread.start();
    }
}
