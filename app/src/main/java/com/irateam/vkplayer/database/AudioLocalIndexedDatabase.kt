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

package com.irateam.vkplayer.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.irateam.vkplayer.database.Tables.AudioLocalIndexed.Columns
import com.irateam.vkplayer.models.LocalAudio
import com.irateam.vkplayer.util.extension.use
import java.util.*

class AudioLocalIndexedDatabase(context: Context) : DatabaseHelper(context) {

    fun insert(audio: LocalAudio): Long = writableDatabase.use {
        it.insert(Tables.AudioLocalIndexed.NAME, null, audio.toContentValues())
    }

    fun update(audio: LocalAudio): Long = writableDatabase.use {
        val id = it.update(Tables.AudioLocalIndexed.NAME,
                audio.toContentValues(),
                "${Columns.PATH} = \"${audio.path}\"",
                null)

        id.toLong()
    }

    fun delete(audio: LocalAudio): Long = writableDatabase.use {
        val id = it.delete(Tables.AudioLocalIndexed.NAME,
                "${Columns.PATH} = \"${audio.path}\"",
                null)

        id.toLong()
    }

    fun index(audio: LocalAudio): Long = writableDatabase.use { db ->
        db.query(Tables.AudioLocalIndexed.NAME,
                null,
                "${Columns.PATH} = \"${audio.path}\"",
                null,
                null,
                null,
                null).use { cursor ->

            if (cursor.count <= 0) {
                insert(audio)
            } else {
                update(audio)
            }
        }
    }

    fun getAll(): List<LocalAudio> = readableDatabase.use { db ->
        db.query(Tables.AudioLocalIndexed.NAME, null, null, null, null, null, "_id DESC")
                .use { cursor ->
                    val audios = ArrayList<LocalAudio>()
                    if (cursor.moveToFirst()) {
                        do {
                            audios.add(cursor.toLocalAudio())
                        } while (cursor.moveToNext())
                    }
                    audios
                }
    }

    fun getIndexedPaths(): Set<String> = readableDatabase.use { db ->
        db.query(Tables.AudioLocalIndexed.NAME, null, null, null, null, null, null)
                .use { cursor ->
                    val paths = HashSet<String>()
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(Columns.PATH)
                        do {
                            paths.add(cursor.getString(columnIndex))
                        } while (cursor.moveToNext())
                    }
                    paths
                }
    }

    fun removeAll(): Unit = writableDatabase.use {
        it.delete(Tables.AudioLocalIndexed.NAME, null, null)
    }


    companion object {

        fun LocalAudio.toContentValues() = ContentValues().apply {
            put(Columns.ARTIST, artist)
            put(Columns.TITLE, title)
            put(Columns.DURATION, duration)
            put(Columns.PATH, path)
        }

        fun Cursor.toLocalAudio(): LocalAudio {
            var i = 1
            val artist = getString(i++)
            val title = getString(i++)
            val duration = getInt(i++)
            val path = getString(i)

            val audio = LocalAudio(
                    artist,
                    title,
                    duration,
                    path)
            return audio
        }
    }
}