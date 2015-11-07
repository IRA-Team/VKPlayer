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

package com.irateam.vkplayer.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.irateam.vkplayer.notifications.PlayerNotificationFactory;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AudioInfo {
    public static final int BUFFER_SIZE = 2048;
    public static final String AUDIO_INFO_PREFIX = "AUDIO_INFO_";

    public int bitrate;
    public long size;
    public ID3v2 tags;
    public Bitmap cover;
    public Bitmap coverNotification;

    private boolean loaded = false;

    private AudioInfo getAudioInfo(Context context, Audio audio) throws Exception {
        Mp3File mp3file = null;
        if (audio.isCached()) {
            mp3file = new Mp3File(audio.getCachePath());
            size = mp3file.getLength();
        } else {
            URLConnection connection = new URL(audio.getUrl()).openConnection();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes;
            size = connection.getContentLength();
            InputStream is = connection.getInputStream();
            File temp = File.createTempFile(AUDIO_INFO_PREFIX, String.valueOf(audio.getId()), context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(temp);
            while ((bytes = is.read(buffer, 0, BUFFER_SIZE)) != -1 && !Thread.interrupted()) {
                outputStream.write(buffer, 0, bytes);
                try {
                    mp3file = new Mp3File(temp.getAbsolutePath());
                    break;
                } catch (InvalidDataException e) {
                    //nothing
                }
            }
            outputStream.close();
        }
        bitrate = mp3file.getBitrate();
        tags = mp3file.getId3v2Tag();

        if (tags != null) {
            byte[] image = tags.getAlbumImage();
            if (image != null) {
                cover = BitmapFactory.decodeByteArray(image, 0, image.length);
                coverNotification = PlayerNotificationFactory.scaleNotification(context, cover);
            }
        }
        loaded = true;
        return this;
    }

    private static ExecutorService sExecutorService = Executors.newSingleThreadExecutor();
    private static Future<AudioInfo> sFuture;
    private static Handler sMainHandler = new Handler(Looper.getMainLooper());

    public void init(Context context, Audio audio) {
        if (sFuture != null && !sFuture.isDone()) {
            sFuture.cancel(true);
        }
        sFuture = sExecutorService.submit(() -> getAudioInfo(context, audio));
    }

    public void getWithListener(AudioInfoListener listener) {
        if (loaded) {
            listener.OnComplete(this);
            return;
        }

        new Thread(() -> {
            try {
                AudioInfo info = sFuture.get();
                sMainHandler.post(() -> listener.OnComplete(info));
            } catch (ExecutionException e) {
                sMainHandler.post(() -> listener.OnError());
            } catch (InterruptedException | CancellationException e) {
                //Nothing
            }
        }).start();
    }

    public interface AudioInfoListener {
        void OnComplete(AudioInfo audioInfo);

        void OnError();
    }
}