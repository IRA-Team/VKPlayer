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

import java.util.*

fun <T> List<T>.swap(from: Int, to: Int): List<T> {
    val toSwap = ArrayList(this)
    Collections.swap(toSwap, from, to)
    return toSwap
}

fun <E> List<E>.splitToPartitions(count: Int): List<List<E>> {
    val elementsCount = Math.ceil(size.toDouble() / count).toInt()
    val slices = ArrayList<List<E>>()
    for (i in 0..count - 1) {
        val probMax = (i + 1) * elementsCount
        val max = if (probMax > size) {
            size
        } else {
            probMax
        }

        val range = IntRange(i * elementsCount, max - 1)
        slices.add(slice(range))
    }
    return slices
}
