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

    fun insert(audio: Audio): Long = writableDatabase.use {
        it.insert(Tables.Audio.NAME, null, audio.toContentValues())
    }


    fun update(audio: Audio): Long = writableDatabase.use {
        val id = it.update(Tables.Audio.NAME, audio.toContentValues(), "id = " + audio.id, null)
        id.toLong()
    }

    fun delete(audio: Audio): Long = writableDatabase.use {
        val id = it.delete(Tables.Audio.NAME, "id = " + audio.id, null)
        id.toLong()
    }

    fun cache(audio: Audio): Long = writableDatabase.use { db ->
        db.query(Tables.Audio.NAME, null, "id = " + audio.id, null, null, null, null)
                .use { cursor ->
                    if (cursor.count <= 0) {
                        insert(audio)
                    } else {
                        update(audio)
                    }
                }
    }

    fun getAll(): List<Audio> = readableDatabase.use { db ->
        db.query(Tables.Audio.NAME, null, null, null, null, null, "_id DESC")
                .use { cursor ->
                    val audios = ArrayList<Audio>()
                    if (cursor.moveToFirst()) {
                        do {
                            audios.add(cursor.toAudio())
                        } while (cursor.moveToNext())
                    }
                    audios
                }
    }

    fun removeAll(): Unit = writableDatabase.use {
        it.delete(Tables.Audio.NAME, null, null)
    }

    companion object {

        fun Audio.toContentValues(): ContentValues {
            val cv = ContentValues()
            cv.put(Tables.Audio.Columns.ID, id)
            cv.put(Tables.Audio.Columns.OWNER_ID, ownerId)
            cv.put(Tables.Audio.Columns.ARTIST, artist)
            cv.put(Tables.Audio.Columns.TITLE, title)
            cv.put(Tables.Audio.Columns.DURATION, duration)
            cv.put(Tables.Audio.Columns.URL, url)
            cv.put(Tables.Audio.Columns.CACHE_PATH, cachePath)
            cv.put(Tables.Audio.Columns.LYRICS_ID, lyricsId)
            cv.put(Tables.Audio.Columns.ALBUM_ID, albumId)
            cv.put(Tables.Audio.Columns.GENRE, genre)
            cv.put(Tables.Audio.Columns.ACCESS_KEY, accessKey)
            return cv
        }

        fun Cursor.toAudio(): Audio {
            var i = 1
            val id = getInt(i++)
            val ownerId = getInt(i++)
            val artist = getString(i++)
            val title = getString(i++)
            val duration = getInt(i++)
            val url = getString(i++)
            val cachePath = getString(i++)
            val lyricsId = getInt(i++)
            val albumId = getInt(i++)
            val genre = getInt(i++)
            val accessKey = getString(i)

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
