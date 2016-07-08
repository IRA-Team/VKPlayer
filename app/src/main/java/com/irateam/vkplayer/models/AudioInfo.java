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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.irateam.vkplayer.notifications.PlayerNotificationFactory;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AudioInfo {

    private int bitrate;
    private long size;
    private ID3v2 tags;
    private Bitmap cover;
    private Bitmap coverNotification;

    public AudioInfo(int bitrate, long size, ID3v2 tags, Bitmap cover, Bitmap coverNotification) {
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