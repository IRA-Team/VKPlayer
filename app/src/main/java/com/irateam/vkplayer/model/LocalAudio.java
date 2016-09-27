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

package com.irateam.vkplayer.model;

import android.os.Parcel;

public class LocalAudio extends Audio {

    private final String path;

    public LocalAudio(String artist,
                      String title,
                      int duration,
                      String path) {

        super(path, artist, title, duration);
        this.path = path;
    }

    protected LocalAudio(Parcel in) {
        super(in);
        this.path = in.readString();
    }

    public String getPath() {
        return path;
    }

    @Override
    public String getSource() {
        return path;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(path);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Audio clone() {
        return null;
    }

    public static Creator<LocalAudio> CREATOR = new Creator<LocalAudio>() {
        public LocalAudio createFromParcel(Parcel source) {
            return new LocalAudio(source);
        }

        public LocalAudio[] newArray(int size) {
            return new LocalAudio[size];
        }
    };
}
