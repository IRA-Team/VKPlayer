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

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class Audio implements Parcelable {

    private final int id;
    private final int ownerId;
    private final String artist;
    private final String title;
    private final int duration;
    private final String url;
    private final int lyricsId;
    private final int albumId;
    private final int genre;
    private final String accessKey;

    private String cachePath;
    private File cacheFile;
    private Metadata metadata;

    public Audio(int id,
                 int ownerId,
                 String artist,
                 String title,
                 int duration,
                 String url,
                 int lyricsId,
                 int albumId,
                 int genre,
                 String accessKey) {

        this.id = id;
        this.ownerId = ownerId;
        this.artist = artist;
        this.title = title;
        this.duration = duration;
        this.url = url;
        this.lyricsId = lyricsId;
        this.albumId = albumId;
        this.genre = genre;
        this.accessKey = accessKey;
    }

    protected Audio(Parcel in) {
        this.id = in.readInt();
        this.ownerId = in.readInt();
        this.artist = in.readString();
        this.title = in.readString();
        this.duration = in.readInt();
        this.url = in.readString();
        this.lyricsId = in.readInt();
        this.albumId = in.readInt();
        this.genre = in.readInt();
        this.accessKey = in.readString();
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
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

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    public Audio clone() {
        Audio audio = new Audio(
                id,
                ownerId,
                artist,
                title,
                duration,
                url,
                lyricsId,
                albumId,
                genre,
                accessKey);

        audio.setCachePath(cachePath);
        return audio;
    }

    @Override
    public String toString() {
        return artist + " - " + title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(ownerId);
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeInt(duration);
        dest.writeString(url);
        dest.writeInt(lyricsId);
        dest.writeInt(albumId);
        dest.writeInt(genre);
        dest.writeString(accessKey);
    }

    public static Creator<Audio> CREATOR = new Creator<Audio>() {
        public Audio createFromParcel(Parcel source) {
            return new Audio(source);
        }

        public Audio[] newArray(int size) {
            return new Audio[size];
        }
    };

}
