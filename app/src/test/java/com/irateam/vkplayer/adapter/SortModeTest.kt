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

package com.irateam.vkplayer.adapter

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SortModeTest {

    @Mock lateinit var listener: SortMode.Listener<Int>
    @Captor lateinit var listCaptor: ArgumentCaptor<List<Int>>

    lateinit var sortMode: SortMode<Int>

    @Before
    fun init() {
        sortMode = SortModeImpl(listener)
    }

    @Test
    fun testSortModeOnStartCallback() {
        sortMode.start()
        verify(listener).onStart()
    }

    @Test
    fun testSortModeOnCommitCallback() {
        sortMode.start()
        verify(listener).onStart()

        sortMode.commit()
        verify(listener).onCommit()
    }

    @Test
    fun testIsSortModeAfterStart() {
        sortMode.start()
        assertTrue(sortMode.isSortMode())
    }

    @Test
    fun testNoSortModeAfterCommit() {
        sortMode.start()
        assertTrue(sortMode.isSortMode())

        sortMode.commit()
        assertFalse(sortMode.isSortMode())
    }

    @Test
    fun testNoSortModeAfterRevert() {
        sortMode.start()
        assertTrue(sortMode.isSortMode())

        sortMode.revert()
        assertFalse(sortMode.isSortMode())
    }

    @Test
    fun testSortModeListenerOnStartCallback() {
        sortMode.start()
        verify(listener).onStart()
    }

    @Test
    fun testSortModeListenerOnCommitCallback() {
        sortMode.commit()
        verify(listener).onCommit()
    }

    @Test
    fun testSortModeListenerOnRevertCallback() {
        sortMode.revert()
        verify(listener).onRevert()
    }

    @Test
    fun testSortModeMove() {
        val toSort = listOf(1, 2, 3, 4, 5)
        `when`(listener.getAudiosToSort()).then { toSort }

        sortMode.move(0, 1)
        verify(listener).onMove(0, 1, listOf(2, 1, 3, 4, 5))

        sortMode.move(1, 0)
        verify(listener).onMove(1, 0, listOf(2, 1, 3, 4, 5))
    }
}