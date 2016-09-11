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

object Tables {

    object AudioVKCache {

        val NAME = "audio_vk_cache"
        val SQL_CREATE = """CREATE TABLE $NAME
                            (_id INTEGER PRIMARY KEY,
                            ${Columns.ID} INTEGER,
                            ${Columns.OWNER_ID} INTEGER,
                            ${Columns.ARTIST} TEXT,
                            ${Columns.TITLE} TEXT,
                            ${Columns.DURATION} INTEGER,
                            ${Columns.URL} TEXT,
                            ${Columns.CACHE_PATH} TEXT,
                            ${Columns.LYRICS_ID} INTEGER,
                            ${Columns.ALBUM_ID} INTEGER,
                            ${Columns.GENRE} INTEGER,
                            ${Columns.ACCESS_KEY} TEXT)"""

        object Columns {
            val ID = "id"
            val OWNER_ID = "owner_id"
            val ARTIST = "artist"
            val TITLE = "title"
            val DURATION = "duration"
            val URL = "url"
            val CACHE_PATH = "cache_path"
            val LYRICS_ID = "lyrics_id"
            val ALBUM_ID = "album_id"
            val GENRE = "genre"
            val ACCESS_KEY = "access_key"
        }
    }

    object AudioLocalIndexed {

        val NAME = "audio_local_indexed"
    }
}