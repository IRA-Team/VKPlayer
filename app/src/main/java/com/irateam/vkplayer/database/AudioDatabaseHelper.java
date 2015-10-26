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
        int id = db.update(TABLE_NAME, toContentValues(audio), "id = " + audio.id, null);
        db.close();
        return id;
    }

    public long delete(Audio audio) {
        SQLiteDatabase db = getWritableDatabase();
        int id = db.delete(TABLE_NAME, "id = " + audio.id, null);
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
        Cursor cursor = db.query(TABLE_NAME, null, "id = " + audio.id, null, null, null, null);
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

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, ID);
        if (cursor.moveToFirst()) {
            do {
                list.add(fromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public static ContentValues toContentValues(Audio audio) {
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
        return cv;
    }

    public static Audio fromCursor(Cursor cursor) {
        int i = 1;
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
        return audio;
    }

}
