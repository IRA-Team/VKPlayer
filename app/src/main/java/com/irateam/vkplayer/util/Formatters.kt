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

package com.irateam.vkplayer.util

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES

object Formatters {

    @JvmStatic
    fun duration(duration: Int): String {
        val value = duration.toLong()
        val minutes = MILLISECONDS.toMinutes(value)
        val seconds = MILLISECONDS.toSeconds(value) - MINUTES.toSeconds(minutes)
        return "%02d:%02d".format(minutes, seconds)
    }

    @JvmStatic
    fun size(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1]
        return "%.1f%sB".format(bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }
}

