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

package com.irateam.vkplayer.api.service

class LocalAudioNameDiscover {

    fun getTitleAndArtist(name: String): ArtistTitle {
        DELIMITERS.forEach {
            splitIfCan(name, it)?.let { return it }
        }

        return ArtistTitle(name, null)
    }

    private fun splitIfCan(name: String, delimiter: String): ArtistTitle? {
        if (delimiter in name) {
            val index = name.indexOf(delimiter)
            val artist = name.substring(0, index)
            val title = name.substring(index + delimiter.length, name.length)
            return ArtistTitle(artist, title)
        } else {
            return null
        }
    }

    data class ArtistTitle(val artist: String?,
                           val title: String?)

    companion object {

        val TAG = LocalAudioNameDiscover::class.java.name
        val DELIMITERS = listOf(" - ", "_-_", " -", "_-", "- ", "-_", "-")
    }
}