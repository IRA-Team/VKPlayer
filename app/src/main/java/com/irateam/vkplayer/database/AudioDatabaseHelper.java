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

package com.irateam.vkplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.irateam.vkplayer.models.Audio;

import java.util.ArrayList;
import java.util.List;

public class AudioDatabaseHelper extends DatabaseHelper {
    public AudioDatabaseHelper(Context context) {
        super(context);
    }

    //Audio table
    public static final String TABLE_NAME = "audios";
    public static final String ID = "id";
    public static final String OWNER_ID = "owner_id";
    public static final String ARTIST = "artist";
    public static final String TITLE = "title";
    public static final String DURATION = "duration";
    public static final String URL = "url";
    public static final String CACHE_PATH = "cache_path";
    public static final String LYRICS_ID = "lyrics_id";
    public static final String ALBUM_ID = "album_id";
    public static final String GENRE = "genre";
    public static final String ACCESS_KEY = "access_key";
    public static final String ORDER_POSITION = "order_position";

    public long insert(Audio audio) {
        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(TABLE_NAME, null, toContentValues(audio));
        db.close();
        return id;
    }

    public long update(Audio audio) {
        SQLiteDatabase db = getWritableDatabase();
        int id = db.update(TABLE_NAME, toContentValues(audio), "id = " + audio.getId(), null);
        db.close();
        return id;
    }

    public long delete(Audio audio) {
        SQLiteDatabase db = getWritableDatabase();
        int id = db.delete(TABLE_NAME, "id = " + audio.getId(), null);
        db.close();
        return id;
    }

    public void delete(List<Audio> list) {
        for (Audio audio : list) {
            delete(audio);
        }
    }

    public long cache(Audio audio) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "id = " + audio.getId(), null, null, null, null);
        long id;
        if (cursor.getCount() <= 0) {
            id = insert(audio);
        } else {
            id = update(audio);
        }
        cursor.close();
        db.close();
        return id;
    }

    public List<Audio> getAll() {
        SQLiteDatabase db = getReadableDatabase();
        List<Audio> list = new ArrayList<>();

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, "_id DESC");
        if (cursor.moveToFirst()) {
            do {
                list.add(fromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public void removeAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    public static ContentValues toContentValues(Audio audio) {
        ContentValues cv = new ContentValues();
        cv.put(ID, audio.getId());
        cv.put(OWNER_ID, audio.getOwnerId());
        cv.put(ARTIST, audio.getArtist());
        cv.put(TITLE, audio.getTitle());
        cv.put(DURATION, audio.getDuration());
        cv.put(URL, audio.getUrl());
        cv.put(CACHE_PATH, audio.getCachePath());
        cv.put(LYRICS_ID, audio.getLyricsId());
        cv.put(ALBUM_ID, audio.getAlbumId());
        cv.put(GENRE, audio.getGenre());
        cv.put(ACCESS_KEY, audio.getAccessKey());
        return cv;
    }

    public static Audio fromCursor(Cursor cursor) {
        int i = 1;
        Audio audio = new Audio();
        audio.setId(cursor.getInt(i++));
        audio.setOwnerId(cursor.getInt(i++));
        audio.setArtist(cursor.getString(i++));
        audio.setTitle(cursor.getString(i++));
        audio.setDuration(cursor.getInt(i++));
        audio.setUrl(cursor.getString(i++));
        audio.setCacheFile(cursor.getString(i++));
        audio.setLyricsId(cursor.getInt(i++));
        audio.setAlbumId(cursor.getInt(i++));
        audio.setGenre(cursor.getInt(i++));
        audio.setAccessKey(cursor.getString(i++));
        return audio;
    }

}
