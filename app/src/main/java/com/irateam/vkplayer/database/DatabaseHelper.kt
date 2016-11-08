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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

abstract class DatabaseHelper : SQLiteOpenHelper {

    constructor(context: Context) : super(context, DATABASE_NAME, null, DATABASE_VERSION)

    override fun onCreate(db: SQLiteDatabase) = with(db) {
        //AudioVKCache
        execSQL(Tables.AudioVKCache.SQL_CREATE)

        //AudioLocalIndexed
        execSQL(Tables.AudioLocalIndexed.SQL_CREATE)
        execSQL(Tables.AudioLocalIndexed.SQL_CREATE_PATH_INDEX)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    companion object {
        val DATABASE_VERSION = 1
        val DATABASE_NAME = "vkplayer.db"
    }
}
