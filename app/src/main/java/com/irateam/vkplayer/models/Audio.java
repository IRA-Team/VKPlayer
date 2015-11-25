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

import org.json.JSONObject;

import java.io.File;

public class Audio implements Parcelable {

    public static final String UNKNOWN_ARTIST = "Unknown artist";
    public static final String UNKNOWN_TITLE = "Unknown title";

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

    public boolean isCached() {
        return cacheFile != null && cacheFile.exists();
    }

    public Audio() {

    }

    public Audio(JSONObject from) {
        parse(from);
    }

    public Audio parse(JSONObject from) {
        id = from.optInt("id");
        ownerId = from.optInt("owner_id");
        setArtist(from.optString("artist"));
        setTitle(from.optString("title"));
        duration = from.optInt("duration");
        url = from.optString("url");
        lyricsId = from.optInt("lyrics_id");
        albumId = from.optInt("album_id");
        genre = from.optInt("genre_id");
        accessKey = from.optString("access_key");
        return this;
    }

    public Audio(Parcel in) {
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

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        artist = artist.trim();
        this.artist = artist.length() > 0 ? artist : UNKNOWN_ARTIST;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        title = title.trim();
        this.title = title.length() > 0 ? title : UNKNOWN_TITLE;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLyricsId() {
        return lyricsId;
    }

    public void setLyricsId(int lyricsId) {
        this.lyricsId = lyricsId;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public int getGenre() {
        return genre;
    }

    public void setGenre(int genre) {
        this.genre = genre;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
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

    public Audio clone() {
        Audio audio = new Audio();
        audio.id = id;
        audio.ownerId = ownerId;
        audio.artist = artist;
        audio.title = title;
        audio.duration = duration;
        audio.url = url;
        audio.lyricsId = lyricsId;
        audio.albumId = albumId;
        audio.genre = genre;
        audio.accessKey = accessKey;
        audio.cacheFile = cacheFile;
        return audio;
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
