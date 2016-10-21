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

import android.util.Log

val DEFAULT_TAG = "VKPlayer Global Log"

//Verbose level
fun v(obj: Any) {
    v(DEFAULT_TAG, obj)
}

fun v(tag: String, obj: Any) {
    Log.v(tag, obj.toString());
}

fun v(tag: String, message: String, vararg objects: Any) {
    Log.v(tag, message.format(objects));
}

//Debug level
fun d(obj: Any) {
    d(DEFAULT_TAG, obj)
}

fun d(tag: String, obj: Any) {
    Log.d(tag, obj.toString());
}

fun d(tag: String, message: String, vararg objects: Any) {
    Log.d(tag, message.format(objects));
}

//Info level
fun i(obj: Any) {
    i(DEFAULT_TAG, obj)
}

fun i(tag: String, obj: Any) {
    Log.i(tag, obj.toString());
}

fun i(tag: String, message: String, vararg objects: Any) {
    Log.i(tag, message.format(objects));
}

//Warning level
fun w(obj: Any) {
    w(DEFAULT_TAG, obj)
}

fun w(tag: String, obj: Any) {
    Log.w(tag, obj.toString());
}

fun w(tag: String, message: String, vararg objects: Any) {
    Log.w(tag, message.format(objects));
}

//Error level
fun e(obj: Any) {
    e(DEFAULT_TAG, obj)
}

fun e(tag: String, obj: Any) {
    Log.e(tag, obj.toString());
}

fun e(tag: String, message: String, vararg objects: Any) {
    Log.e(tag, message.format(objects));
}

//Wtf level
fun wtf(obj: Any) {
    wtf(DEFAULT_TAG, obj)
}

fun wtf(tag: String, obj: Any) {
    Log.wtf(tag, obj.toString());
}

fun wtf(tag: String, message: String, vararg objects: Any) {
    Log.wtf(tag, message.format(objects));
}