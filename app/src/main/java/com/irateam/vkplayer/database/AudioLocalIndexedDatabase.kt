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
import com.irateam.vkplayer.model.LocalAudio
import com.irateam.vkplayer.util.extension.*
import java.util.*

class AudioLocalIndexedDatabase(context: Context) : DatabaseHelper(context) {

    fun insert(audio: LocalAudio): Long = writableDatabase.use { db ->
        db.insert(
                table = Tables.AudioLocalIndexed.NAME,
                values = audio.toContentValues())
    }

    fun update(audio: LocalAudio): Long = writableDatabase.use { db ->
        db.update(
                table = Tables.AudioLocalIndexed.NAME,
                values = audio.toContentValues(),
                whereClause = "${Columns.PATH} = \"${audio.path}\"")
    }

    fun delete(audio: LocalAudio): Long = writableDatabase.use {
        it.delete(
                table = Tables.AudioLocalIndexed.NAME,
                whereClause = "${Columns.PATH} = \"${audio.path}\"")
    }

    fun index(audio: LocalAudio): Long = writableDatabase.use { db ->
        val cursor = db.query(
                table = Tables.AudioLocalIndexed.NAME,
                selection = "${Columns.PATH} = \"${audio.path}\"")

        cursor.use {
            if (it.count <= 0) {
                insert(audio)
            } else {
                update(audio)
            }
        }
    }

    fun bulkIndex(audios: List<LocalAudio>) = writableDatabase.use { db ->
        db.beginTransaction()

        audios.forEach { audio ->
            val cursor = db.query(
                    table = Tables.AudioLocalIndexed.NAME,
                    selection = "${Columns.PATH} = \"${audio.path}\"")

            cursor.use {
                if (it.count <= 0) {
                    db.insert(
                            table = Tables.AudioLocalIndexed.NAME,
                            values = audio.toContentValues())
                } else {
                    db.update(
                            table = Tables.AudioLocalIndexed.NAME,
                            values = audio.toContentValues(),
                            whereClause = "${Columns.PATH} = \"${audio.path}\"")
                }
            }
        }

        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun getAll(): List<LocalAudio> = readableDatabase.use { db ->
        db.query(table = Tables.AudioLocalIndexed.NAME).use { cursor ->
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
        db.query(table = Tables.AudioLocalIndexed.NAME).use { cursor ->
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
        it.delete(table = Tables.AudioLocalIndexed.NAME)
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
            return LocalAudio(
                    artist = getString(i++),
                    title = getString(i++),
                    duration = getInt(i++),
                    path = getString(i))
        }
    }
}