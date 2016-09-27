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

import android.graphics.Bitmap;

import com.mpatric.mp3agic.ID3v2;

public class Metadata {

    private int bitrate;
    private long size;
    private ID3v2 tags;
    private Bitmap cover;
    private Bitmap coverNotification;

    public Metadata(int bitrate, long size, ID3v2 tags, Bitmap cover, Bitmap coverNotification) {
        this.bitrate = bitrate;
        this.size = size;
        this.tags = tags;
        this.cover = cover;
        this.coverNotification = coverNotification;
    }

    public int getBitrate() {
        return bitrate;
    }

    public long getSize() {
        return size;
    }

    public ID3v2 getTags() {
        return tags;
    }

    public Bitmap getCover() {
        return cover;
    }

    public Bitmap getCoverNotification() {
        return coverNotification;
    }
}