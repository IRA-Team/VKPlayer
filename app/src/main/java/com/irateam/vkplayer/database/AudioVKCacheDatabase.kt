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
import com.irateam.vkplayer.model.VKAudio
import com.irateam.vkplayer.util.extension.use
import java.util.*

class AudioVKCacheDatabase(context: Context) : DatabaseHelper(context) {

    fun insert(audio: VKAudio): Long = writableDatabase.use {
        it.insert(Tables.AudioVKCache.NAME, null, audio.toContentValues())
    }

    fun update(audio: VKAudio): Long = writableDatabase.use {
        val id = it.update(
                Tables.AudioVKCache.NAME,
                audio.toContentValues(),
                idWhereClause(audio),
                null)
        id.toLong()
    }

    fun delete(audio: VKAudio): Long = writableDatabase.use {
        val id = it.delete(Tables.AudioVKCache.NAME, idWhereClause(audio), null)
        id.toLong()
    }

    fun cache(audio: VKAudio): Long = writableDatabase.use { db ->
        db.query(Tables.AudioVKCache.NAME, null, idWhereClause(audio), null, null, null, null)
                .use { cursor ->
                    if (cursor.count <= 0) {
                        insert(audio)
                    } else {
                        update(audio)
                    }
                }
    }

    fun getAll(): List<VKAudio> = readableDatabase.use { db ->
        db.query(Tables.AudioVKCache.NAME, null, null, null, null, null, "_id DESC")
                .use { cursor ->
                    val audios = ArrayList<VKAudio>()
                    if (cursor.moveToFirst()) {
                        do {
                            audios.add(cursor.toAudio())
                        } while (cursor.moveToNext())
                    }
                    audios
                }
    }

    fun removeAll(): Unit = writableDatabase.use {
        it.delete(Tables.AudioVKCache.NAME, null, null)
    }

    private fun idWhereClause(audio: VKAudio): String {
        return "${Tables.AudioVKCache.Columns.AUDIO_ID} = ${audio.audioId} AND " +
                "${Tables.AudioVKCache.Columns.OWNER_ID} = ${audio.ownerId}"
    }

    companion object {

        fun VKAudio.toContentValues(): ContentValues {
            val cv = ContentValues()
            cv.put(Tables.AudioVKCache.Columns.AUDIO_ID, audioId)
            cv.put(Tables.AudioVKCache.Columns.OWNER_ID, ownerId)
            cv.put(Tables.AudioVKCache.Columns.ARTIST, artist)
            cv.put(Tables.AudioVKCache.Columns.TITLE, title)
            cv.put(Tables.AudioVKCache.Columns.DURATION, duration)
            cv.put(Tables.AudioVKCache.Columns.URL, url)
            cv.put(Tables.AudioVKCache.Columns.CACHE_PATH, cachePath)
            cv.put(Tables.AudioVKCache.Columns.LYRICS_ID, lyricsId)
            cv.put(Tables.AudioVKCache.Columns.ALBUM_ID, albumId)
            cv.put(Tables.AudioVKCache.Columns.GENRE, genre)
            cv.put(Tables.AudioVKCache.Columns.ACCESS_KEY, accessKey)
            return cv
        }

        fun Cursor.toAudio(): VKAudio {
            var i = 1
            val audioId = getInt(i++)
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

            val audio = VKAudio(
                    audioId,
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
