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

package com.irateam.vkplayer.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.irateam.vkplayer.models.Audio
import java.util.*

class AudioDatabaseHelper(context: Context) : DatabaseHelper(context) {

    fun insert(audio: Audio): Long {
        val db = writableDatabase
        val id = db.insert(Tables.Audio.NAME, null, toContentValues(audio))
        db.close()
        return id
    }

    fun update(audio: Audio): Long {
        val db = writableDatabase
        val id = db.update(Tables.Audio.NAME, toContentValues(audio), "id = " + audio.id, null)
        db.close()
        return id.toLong()
    }

    fun delete(audio: Audio): Long {
        val db = writableDatabase
        val id = db.delete(Tables.Audio.NAME, "id = " + audio.id, null)
        db.close()
        return id.toLong()
    }

    fun delete(list: List<Audio>) {
        for (audio in list) {
            delete(audio)
        }
    }

    fun cache(audio: Audio): Long {
        val db = writableDatabase
        val cursor = db.query(Tables.Audio.NAME, null, "id = " + audio.id, null, null, null, null)
        val id: Long
        if (cursor.count <= 0) {
            id = insert(audio)
        } else {
            id = update(audio)
        }
        cursor.close()
        db.close()
        return id
    }

    val all: List<Audio>
        get() {
            val db = readableDatabase
            val list = ArrayList<Audio>()

            val cursor = db.query(Tables.Audio.NAME, null, null, null, null, null, "_id DESC")
            if (cursor.moveToFirst()) {
                do {
                    list.add(fromCursor(cursor))
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            return list
        }

    fun removeAll() {
        val db = writableDatabase
        db.delete(Tables.Audio.NAME, null, null)
        db.close()
    }

    companion object {

        fun toContentValues(audio: Audio): ContentValues {
            val cv = ContentValues()
            cv.put(Tables.Audio.Columns.ID, audio.id)
            cv.put(Tables.Audio.Columns.OWNER_ID, audio.ownerId)
            cv.put(Tables.Audio.Columns.ARTIST, audio.artist)
            cv.put(Tables.Audio.Columns.TITLE, audio.title)
            cv.put(Tables.Audio.Columns.DURATION, audio.duration)
            cv.put(Tables.Audio.Columns.URL, audio.url)
            cv.put(Tables.Audio.Columns.CACHE_PATH, audio.cachePath)
            cv.put(Tables.Audio.Columns.LYRICS_ID, audio.lyricsId)
            cv.put(Tables.Audio.Columns.ALBUM_ID, audio.albumId)
            cv.put(Tables.Audio.Columns.GENRE, audio.genre)
            cv.put(Tables.Audio.Columns.ACCESS_KEY, audio.accessKey)
            return cv
        }

        fun fromCursor(cursor: Cursor): Audio {
            var i = 1
            val id = cursor.getInt(i++)
            val ownerId = cursor.getInt(i++)
            val artist = cursor.getString(i++)
            val title = cursor.getString(i++)
            val duration = cursor.getInt(i++)
            val url = cursor.getString(i++)
            val cachePath = cursor.getString(i++)
            val lyricsId = cursor.getInt(i++)
            val albumId = cursor.getInt(i++)
            val genre = cursor.getInt(i++)
            val accessKey = cursor.getString(i)

            val audio = Audio(
                    id,
                    ownerId,
                    artist,
                    title,
                    duration,
                    url,
                    lyricsId,
                    albumId,
                    genre,
                    accessKey)
            audio.cachePath = cachePath
            return audio
        }
    }

}
