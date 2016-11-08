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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LocalAudioNameDiscoverTest {

    lateinit var service: LocalAudioNameDiscover

    @Before
    fun init() {
        service = LocalAudioNameDiscover()
    }

    @Test
    fun testSimpleAudioName() {
        val name = "KoRn - Twisted Transistor"
        val artistTitle = service.getTitleAndArtist(name)
        assertEquals(artistTitle.artist, "KoRn")
        assertEquals(artistTitle.title, "Twisted Transistor")
    }

    @Test
    fun testUnderscoredAudioName() {
        val name = "Metallica_-_The_Unforgiven"
        val artistTitle = service.getTitleAndArtist(name)
        assertEquals(artistTitle.artist, "Metallica")
        assertEquals(artistTitle.title, "The_Unforgiven")
    }

    @Test
    fun testAudioNameWithTwoDelimiters() {
        val name = "Red Hot Chili Peppers - Snow - Hey Oh"
        val artistTitle = service.getTitleAndArtist(name)
        assertEquals(artistTitle.artist, "Red Hot Chili Peppers")
        assertEquals(artistTitle.title, "Snow - Hey Oh")
    }

    @Test
    fun testAudioNameWithNoDelimiters() {
        val name = "Pink Floyd Time"
        val artistTitle = service.getTitleAndArtist(name)
        println(artistTitle)
        assertNull(artistTitle.artist)
        assertEquals(artistTitle.title, "Pink Floyd Time")
    }
}
