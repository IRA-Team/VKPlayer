/*
 * Copyright (C) 2016 IRA-Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irateam.vkplayer.models;

import android.os.Parcel;

import java.io.File;

public class VKAudio extends Audio {

    private final int ownerId;
    private final String url;
    private final int lyricsId;
    private final int albumId;
    private final int genre;
    private final String accessKey;

    private String cachePath;
    private File cacheFile;

    public VKAudio(String id,
                   int ownerId,
                   String artist,
                   String title,
                   int duration,
                   String url,
                   int lyricsId,
                   int albumId,
                   int genre,
                   String accessKey) {
        super(id, artist, title, duration);
        this.ownerId = ownerId;
        this.url = url;
        this.lyricsId = lyricsId;
        this.albumId = albumId;
        this.genre = genre;
        this.accessKey = accessKey;
    }

    protected VKAudio(Parcel in) {
        super(in);
        this.ownerId = in.readInt();
        this.url = in.readString();
        this.lyricsId = in.readInt();
        this.albumId = in.readInt();
        this.genre = in.readInt();
        this.accessKey = in.readString();
    }


    public int getOwnerId() {
        return ownerId;
    }

    public String getUrl() {
        return url;
    }

    public int getLyricsId() {
        return lyricsId;
    }

    public int getAlbumId() {
        return albumId;
    }

    public int getGenre() {
        return genre;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getCachePath() {
        return cachePath;
    }

    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
        if (cachePath != null && !cachePath.isEmpty()) {
            this.cacheFile = new File(cachePath);
        }
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public boolean isCached() {
        return cacheFile != null && cacheFile.exists();
    }

    public void removeFromCache() {
        if (cacheFile != null && cacheFile.delete()) {
            cachePath = null;
        }
    }

    public String getSource() {
        return isCached() ? getCachePath() : getUrl();
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    public VKAudio clone() {
        VKAudio audio = new VKAudio(
                getId(),
                getOwnerId(),
                getArtist(),
                getTitle(),
                getDuration(),
                getUrl(),
                getLyricsId(),
                getAlbumId(),
                getGenre(),
                getAccessKey());

        audio.setCachePath(cachePath);
        return audio;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(ownerId);
        dest.writeString(url);
        dest.writeInt(lyricsId);
        dest.writeInt(albumId);
        dest.writeInt(genre);
        dest.writeString(accessKey);
    }

    public static Creator<VKAudio> CREATOR = new Creator<VKAudio>() {
        public VKAudio createFromParcel(Parcel source) {
            return new VKAudio(source);
        }

        public VKAudio[] newArray(int size) {
            return new VKAudio[size];
        }
    };

}
