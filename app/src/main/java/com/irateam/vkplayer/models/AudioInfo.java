package com.irateam.vkplayer.models;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class AudioInfo {
    public static final int BUFFER_SIZE = 2048;
    public static final String AUDIO_INFO_PREFIX = "AUDIO_INFO_";

    public int bitrate;
    public int size;
    public ID3v2 tags;
    public Bitmap cover;

    public static AudioInfo getAudioInfo(Context context, Audio audio) throws Exception {
        AudioInfo info = new AudioInfo();
        Mp3File mp3file = null;
        if (audio.isCached()) {
            mp3file = new Mp3File(audio.getCachePath());
        } else {
            URLConnection connection = new URL(audio.getUrl()).openConnection();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes;
            info.size = connection.getContentLength();
            InputStream is = connection.getInputStream();
            File temp = File.createTempFile(AUDIO_INFO_PREFIX, String.valueOf(audio.getId()), context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(temp);
            while ((bytes = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
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
        info.bitrate = mp3file.getBitrate();
        info.tags = mp3file.getId3v2Tag();

        if (info.tags != null) {
            byte[] image = info.tags.getAlbumImage();
            if (image != null) {
                info.cover = BitmapFactory.decodeByteArray(image, 0, image.length);
            }
        }
        return info;
    }

    public static void load(Context context, Audio audio, AudioInfoListener listener) {
        new Thread(() -> {
            try {
                AudioInfo info = getAudioInfo(context, audio);
                new Handler(Looper.getMainLooper()).post(() -> listener.OnComplete(info));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public interface AudioInfoListener {
        void OnComplete(AudioInfo info);
    }


}