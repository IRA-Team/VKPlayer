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

import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.MotionEvent
import com.irateam.vkplayer.adapter.event.BaseAudioAdapterEvent
import com.irateam.vkplayer.model.Audio
import com.irateam.vkplayer.player.*
import com.irateam.vkplayer.ui.ItemTouchHelperAdapter
import com.irateam.vkplayer.ui.SimpleItemTouchHelperCallback
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder
import org.greenrobot.eventbus.Subscribe
import java.util.*

abstract class BaseAudioRecyclerAdapter<A : Audio, VH : RecyclerView.ViewHolder> :
		RecyclerView.Adapter<VH>(),
		SortMode.Listener<A>,
		ItemTouchHelperAdapter {

	protected val sortModeDelegate: SortMode<A> = SortModeImpl(this)
	protected abstract val searchDelegate: SearchDelegate
	protected val itemTouchHelper: ItemTouchHelper

	protected var recyclerView: RecyclerView? = null

	abstract var audios: List<A>
	abstract var checkedAudios: HashSet<A>
	var checkedListener: CheckedListener? = null

	init {
		val callback = SimpleItemTouchHelperCallback(this)
		this.itemTouchHelper = ItemTouchHelper(callback)
	}

	override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
		this.recyclerView = recyclerView
		itemTouchHelper.attachToRecyclerView(recyclerView)
	}

	override fun onBindViewHolder(holder: VH, position: Int) {
		throw UnsupportedOperationException("not implemented")
	}

	protected fun configurePlayingState(holder: AudioViewHolder, audio: A) {
		val playingAudio = Player.audio
		if (audio.id == playingAudio?.id) {
			val state = when {
				!Player.isReady -> AudioViewHolder.State.PREPARE
				Player.isReady && Player.isPlaying -> AudioViewHolder.State.PLAY
				Player.isReady && !Player.isPlaying -> AudioViewHolder.State.PAUSE
				else -> AudioViewHolder.State.NONE
			}
			holder.setPlayingState(state)
		}
	}

	protected fun configureCheckedState(holder: AudioViewHolder, audio: A) {
		holder.setChecked(audio in checkedAudios)
		setupCheckedClickListener(holder, audio)
	}

	protected fun configureSortMode(holder: AudioViewHolder) {
		holder.setSorting(isSortMode())
		if (isSortMode()) {
			setupDragTouchListener(holder)
		}
	}

	protected fun setupCheckedClickListener(holder: AudioViewHolder, audio: A) {
		holder.coverHolder.setOnTouchListener(null)
		holder.coverHolder.setOnClickListener {
			holder.toggleChecked(shouldAnimate = true)
			if (holder.isChecked()) checkedAudios.add(audio) else checkedAudios.remove(audio)
			checkedListener?.onChanged(audio, checkedAudios)
		}
	}

	protected fun setupDragTouchListener(holder: AudioViewHolder) {
		holder.coverHolder.setOnClickListener(null)
		holder.coverHolder.setOnTouchListener { v, e ->
			if (MotionEventCompat.getActionMasked(e) == MotionEvent.ACTION_DOWN) {
				itemTouchHelper.startDrag(holder)
			}
			false
		}
	}

	protected fun scrollToTop() {
		recyclerView?.layoutManager?.scrollToPosition(0)
	}

	fun startSortMode() {
		sortModeDelegate.start()
	}

	fun commitSortMode() {
		sortModeDelegate.commit()
	}

	fun revertSortMode() {
		sortModeDelegate.revert()
		scrollToTop()
	}

	fun isSortMode(): Boolean {
		return sortModeDelegate.isSortMode()
	}

	fun sort(comparators: Pair<Comparator<in A>, Comparator<in A>>) {
		sortModeDelegate.sort(comparators)
		scrollToTop()
	}

	fun setSearchQuery(query: String) {
		searchDelegate.search(query)
	}

	abstract fun removeChecked()

	abstract fun clearChecked()

	override fun onStart() {
		notifyItemRangeChanged(0, itemCount, BaseAudioAdapterEvent.SortModeStarted)
	}

	override fun onMove(from: Int, to: Int, newList: List<A>) {
		audios = newList
		notifyItemMoved(from, to)
	}

	override fun getAudiosToSort(): List<A> {
		return audios
	}

	override fun onCommit() {
		notifyItemRangeChanged(0, itemCount, BaseAudioAdapterEvent.SortModeFinished)
	}

	override fun onRevert() {
		notifyItemRangeChanged(0, itemCount, BaseAudioAdapterEvent.SortModeFinished)
	}

	@Subscribe
	fun onStartEvent(e: PlayerStartEvent) {
		notifyEvent(e)
	}

	@Subscribe
	fun onPlayEvent(e: PlayerPlayEvent) {
		notifyEvent(e)
	}

	@Subscribe
	fun onResumeEvent(e: PlayerResumeEvent) {
		notifyEvent(e)
	}

	@Subscribe
	fun onPauseEvent(e: PlayerPauseEvent) {
		notifyEvent(e)
	}

	private fun notifyEvent(e: PlayerEvent) {
		notifyDataSetChanged()
	}

	interface CheckedListener {

		fun onChanged(audio: Audio, checked: HashSet<out Audio>)
	}
}