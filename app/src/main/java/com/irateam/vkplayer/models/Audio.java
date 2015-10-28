package com.irateam.vkplayer.models;

import android.os.Parcel;

import com.vk.sdk.api.model.VKApiAudio;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Audio extends VKApiAudio implements Cloneable {
    public String cachePath;

    public boolean isCached() {
        return cachePath != null && new File(cachePath).exists();
    }

    public Audio() {

    }

    public Audio(JSONObject from) {
        parse(from);
    }

    public Audio parse(JSONObject from) {
        super.parse(from);
        return this;
    }

    public String getPlayingUrl() {
        if (isCached()) {
            return cachePath;
        } else {
            return url;
        }
    }

    public Audio(Parcel in) {
        super(in);
        this.cachePath = in.readString();
    }

    public Long getSize() throws IOException {
        if (isCached()) {
            return new File(getPlayingUrl()).length();
        } else {
            return (long) new URL(url).openConnection().getContentLength();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(cachePath);
    }

    public static Creator<Audio> CREATOR = new Creator<Audio>() {
        public Audio createFromParcel(Parcel source) {
            return new Audio(source);
        }

        public Audio[] newArray(int size) {
            return new Audio[size];
        }
    };

    public Audio clone() {
        Audio audio = new Audio();
        audio.id = id;
        audio.owner_id = owner_id;
        audio.artist = artist;
        audio.title = title;
        audio.duration = duration;
        audio.url = url;
        audio.lyrics_id = lyrics_id;
        audio.album_id = album_id;
        audio.genre = genre;
        audio.access_key = access_key;
        audio.cachePath = cachePath;
        return audio;
    }
}
