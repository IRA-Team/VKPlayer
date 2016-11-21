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

package com.irateam.vkplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public abstract class Audio implements Parcelable {

    private final String id;
    private final String artist;
    private final String title;
    private final int duration;
    private Metadata metadata;

    public Audio(String id,
                 String artist,
                 String title,
                 int duration) {

        this.id = id;
        this.artist = artist;
        this.title = title;
        this.duration = duration;
    }

    protected Audio(Parcel in) {
        this.id = in.readString();
        this.artist = in.readString();
        this.title = in.readString();
        this.duration = in.readInt();
    }

    public String getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return getArtist() + " - " + getTitle();
    }

    public int getDuration() {
        return duration;
    }

    public abstract String getSource();

    public boolean isLocal() {
        return new File(getSource()).exists();
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public abstract Audio clone();

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeInt(duration);
    }

}
