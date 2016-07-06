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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;

public class Audio implements Parcelable {

    private int id;
    private int ownerId;
    private String artist;
    private String title;
    private int duration;
    private String url;
    private int lyricsId;
    private int albumId;
    private int genre;
    private String accessKey;
    private File cacheFile;
    private AudioInfo audioInfo = new AudioInfo();

    @JsonCreator
    public Audio(@JsonProperty("id") int id,
                 @JsonProperty("owner_id") int ownerId,
                 @JsonProperty("artist") String artist,
                 @JsonProperty("title") String title,
                 @JsonProperty("duration") int duration,
                 @JsonProperty("url") String url,
                 @JsonProperty("lyrics_id") int lyricsId,
                 @JsonProperty("album_id") int albumId,
                 @JsonProperty("genre_id") int genre,
                 @JsonProperty("access_key") String accessKey) {

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

    private Audio(Parcel in) {
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
        this.setCacheFile(in.readString());
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
        return cacheFile != null ? cacheFile.getAbsolutePath() : "";
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    public void setCacheFile(String cachePath) {
        cacheFile = !cachePath.isEmpty() ? new File(cachePath) : null;
    }

    public void removeCacheFile() {
        cacheFile.delete();
        cacheFile = null;
    }

    public String getPlayingUrl() {
        return isCached() ? getCachePath() : url;
    }

    public AudioInfo getAudioInfo() {
        return audioInfo;
    }

    public void setAudioInfo(AudioInfo audioInfo) {
        this.audioInfo = audioInfo;
    }

    public boolean equalsId(Audio audio) {
        return id == audio.getId();
    }

    public boolean isCached() {
        return cacheFile != null && cacheFile.exists();
    }

    public Audio clone() {
        return new Audio(
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
        dest.writeString(getCachePath());
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
