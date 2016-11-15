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

package com.irateam.vkplayer.util.extension

import android.content.SharedPreferences
import com.irateam.vkplayer.util.SharedPreferencesProvider
import java.sql.Time
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun SharedPreferences.save(key: String, value: String) {
    edit().putString(key, value).commit()
}

fun SharedPreferences.save(key: String, value: Boolean) {
    edit().putBoolean(key, value).commit()
}

fun SharedPreferences.save(key: String, value: Int) {
    edit().putInt(key, value).commit()
}

fun SharedPreferences.save(key: String, value: Long) {
    edit().putLong(key, value).commit()
}

object SharedPreferencesDelegates {

    fun string(defaultValue: String) = StringPreferencesProperty(defaultValue)

    fun boolean(defaultValue: Boolean) = BooleanPreferencesProperty(defaultValue)

    fun int(defaultValue: Int) = IntPreferencesProperty(defaultValue)

    fun time(defaultValue: Long) = TimePreferencesProperty(defaultValue)

    fun <T> custom(defaultValue: T, mapperFrom: (String) -> T) = CustomPreferencesProperty<T>(defaultValue, mapperFrom)
}

class StringPreferencesProperty : ReadWriteProperty<SharedPreferencesProvider, String> {

    val defaultValue: String

    constructor(defaultValue: String) {
        this.defaultValue = defaultValue
    }

    override fun getValue(thisRef: SharedPreferencesProvider, property: KProperty<*>): String {
        val key = property.name
        return thisRef.sharedPreferences.getString(key, defaultValue)
    }

    override fun setValue(thisRef: SharedPreferencesProvider, property: KProperty<*>, value: String) {
        val key = property.name
        thisRef.sharedPreferences.save(key, value)
    }
}

class BooleanPreferencesProperty : ReadWriteProperty<SharedPreferencesProvider, Boolean> {

    val defaultValue: Boolean

    constructor(defaultValue: Boolean) {
        this.defaultValue = defaultValue
    }

    override fun getValue(thisRef: SharedPreferencesProvider, property: KProperty<*>): Boolean {
        val key = property.name
        return thisRef.sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun setValue(thisRef: SharedPreferencesProvider, property: KProperty<*>, value: Boolean) {
        val key = property.name
        thisRef.sharedPreferences.save(key, value)
    }

}

class IntPreferencesProperty : ReadWriteProperty<SharedPreferencesProvider, Int> {

    val defaultValue: Int

    constructor(defaultValue: Int) {
        this.defaultValue = defaultValue
    }

    override fun getValue(thisRef: SharedPreferencesProvider, property: KProperty<*>): Int {
        val key = property.name
        return thisRef.sharedPreferences.getInt(key, defaultValue)
    }

    override fun setValue(thisRef: SharedPreferencesProvider, property: KProperty<*>, value: Int) {
        val key = property.name
        thisRef.sharedPreferences.save(key, value)
    }
}

class TimePreferencesProperty : ReadWriteProperty<SharedPreferencesProvider, Time> {

    val defaultValue: Long

    constructor(defaultValue: Long) {
        this.defaultValue = defaultValue
    }

    override fun getValue(thisRef: SharedPreferencesProvider, property: KProperty<*>): Time {
        val key = property.name
        return Time(thisRef.sharedPreferences.getLong(key, defaultValue))
    }

    override fun setValue(thisRef: SharedPreferencesProvider, property: KProperty<*>, value: Time) {
        val key = property.name
        thisRef.sharedPreferences.save(key, value.time)
    }

}

class CustomPreferencesProperty<T> : ReadWriteProperty<SharedPreferencesProvider, T> {

    val defaultValue: T
    val defaultValueRaw: String

    val mapperTo: (String) -> T
    val mapperFrom: (T) -> String

    constructor(defaultValue: T, mapperTo: (String) -> T) : this(defaultValue, mapperTo, { it.toString() })

    constructor(defaultValue: T, mapperTo: (String) -> T, mapperFrom: (T) -> String) {
        this.defaultValue = defaultValue
        this.mapperTo = mapperTo
        this.mapperFrom = mapperFrom
        this.defaultValueRaw = mapperFrom(defaultValue)
    }

    override fun getValue(thisRef: SharedPreferencesProvider, property: KProperty<*>): T {
        val key = property.name
        return mapperTo(thisRef.sharedPreferences.getString(key, defaultValueRaw))
    }

    override fun setValue(thisRef: SharedPreferencesProvider, property: KProperty<*>, value: T) {
        val key = property.name
        thisRef.sharedPreferences.save(key, mapperFrom(value))
    }
}