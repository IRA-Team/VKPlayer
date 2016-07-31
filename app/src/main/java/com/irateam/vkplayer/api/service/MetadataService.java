package com.irateam.vkplayer.api.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.irateam.vkplayer.api.AbstractQuery;
import com.irateam.vkplayer.api.Query;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.models.Metadata;
import com.irateam.vkplayer.notifications.PlayerNotificationFactory;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public final class MetadataService {

    public static final int BUFFER_SIZE = 2048;
    public static final String AUDIO_INFO_PREFIX = "AUDIO_INFO_";

    private final Context context;

    public MetadataService(Context context) {
        this.context = context;
    }

    public Query<Metadata> get(Audio audio) {
        return new GetMetadataQuery(audio);
    }

    private class GetMetadataQuery extends AbstractQuery<Metadata> {

        private final Audio audio;

        public GetMetadataQuery(Audio audio) {
            this.audio = audio;
        }

        @Override
        protected Metadata query() throws Exception {
            int bitrate;
            long size;
            ID3v2 tags;
            Bitmap cover = null;
            Bitmap coverNotification = null;

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
                    } catch (InvalidDataException ignore) {
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

            return new Metadata(
                    bitrate,
                    size,
                    tags,
                    cover,
                    coverNotification);
        }
    }
}
