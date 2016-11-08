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

import com.irateam.vkplayer.util.extension.swap
import java.util.*

class SortModeImpl<A> : SortMode<A> {

	private val listener: SortMode.Listener<A>

	private var sortMode = false
	private var original: List<A> = emptyList()

	constructor(listener: SortMode.Listener<A>) {
		this.listener = listener
	}

	override fun start() {
		this.sortMode = true
		listener.onStart()
		this.original = listener.getAudiosToSort()
	}

	override fun sort(comparators: Pair<Comparator<in A>, Comparator<in A>>) {
		val toSort = listener.getAudiosToSort()
		val pending = ArrayList(toSort)

		val sorted = toSort.sortedWith(comparators.first).let {
			if (it == toSort) toSort.sortedWith(comparators.second) else it
		}

		sorted.forEachIndexed { index, item ->
			val from = pending.indexOf(item)
			pending.removeAt(from)
			pending.add(index, item)
			listener.onMove(from, index, pending)
		}
	}

	override fun move(from: Int, to: Int) {
		listener.onMove(from, to, listener.getAudiosToSort().swap(from, to))
	}

	override fun commit() {
		this.sortMode = false
		listener.onCommit()
	}

	override fun revert() {
		sortMode = false
		val pending = listener.getAudiosToSort().toMutableList()
		original.forEachIndexed { index, item ->
			val from = pending.indexOf(item)
			pending.removeAt(from)
			pending.add(index, item)

			listener.onMove(from, index, pending)
		}
		listener.onRevert()
	}

	override fun isSortMode(): Boolean {
		return sortMode
	}
}