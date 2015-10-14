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
    public static final String ID = "_id";
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

    public long insert(Audio audio) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ID, audio.id);
        cv.put(OWNER_ID, audio.owner_id);
        cv.put(ARTIST, audio.artist);
        cv.put(TITLE, audio.title);
        cv.put(DURATION, audio.duration);
        cv.put(URL, audio.url);
        cv.put(CACHE_PATH, audio.cachePath);
        cv.put(LYRICS_ID, audio.lyrics_id);
        cv.put(ALBUM_ID, audio.album_id);
        cv.put(GENRE, audio.genre);
        cv.put(ACCESS_KEY, audio.access_key);
        return db.insert(TABLE_NAME, null, cv);
    }

    public List<Audio> getAll() {
        SQLiteDatabase db = getReadableDatabase();
        List<Audio> list = new ArrayList<>();

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int i = 0;
                Audio audio = new Audio();
                audio.id = cursor.getInt(i++);
                audio.owner_id = cursor.getInt(i++);
                audio.artist = cursor.getString(i++);
                audio.title = cursor.getString(i++);
                audio.duration = cursor.getInt(i++);
                audio.url = cursor.getString(i++);
                audio.cachePath = cursor.getString(i++);
                audio.lyrics_id = cursor.getInt(i++);
                audio.album_id = cursor.getInt(i++);
                audio.genre = cursor.getInt(i++);
                audio.access_key = cursor.getString(i++);
                list.add(audio);
            } while (cursor.moveToNext());
        }
        return list;
    }
}
