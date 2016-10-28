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

package com.irateam.vkplayer.player

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.irateam.vkplayer.model.Audio
import com.irateam.vkplayer.player.Player.RepeatState.*
import com.irateam.vkplayer.util.EventBus
import com.irateam.vkplayer.util.extension.i
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.properties.Delegates.observable

object Player : MediaPlayer(),
		MediaPlayer.OnCompletionListener,
		MediaPlayer.OnPreparedListener,
		MediaPlayer.OnBufferingUpdateListener {

	val TAG: String = Player::class.java.name

	/**
	 * Original playlist.
	 * Inner playlist is built based on original playlist.
	 * Also uses for providing correct audio position.
	 * Must be changed only by calling setQueue()
	 */
	var originalPlaylist: List<Audio> = emptyList()
		private set

	/**
	 * Inner playlist
	 */
	var playlist: List<Audio> = emptyList()
		private set

	/**
	 * Holds audios in order in which they would be played
	 */
	private var queue: MutableList<Audio> = ArrayList()

	/**
	 * Holds audios that should be played next
	 */
	private val playNextQueue: Queue<Audio> = ConcurrentLinkedQueue<Audio>()

	/**
	 * Holds all previously played audios
	 */
	private val historyStack: Stack<Audio> = Stack()

	/**
	 * Currently playing audio
	 */
	var audio: Audio? = null
		private set

	val queueSize: Int
		get() = queue.size

	val audioIndex: Int
		get() = queue.indexOf(audio)

	/**
	 * Position of playing audio relatively to original playlist
	 */
	val audioPosition: Int
		get() = originalPlaylist.indexOf(audio)

	/**
	 * Checks if playing audio is first in queue
	 */
	private val isFirst: Boolean
		get() = audio === queue.first()

	/**
	 * Checks if playing audio is last in queue
	 */
	private val isLast: Boolean
		get() = audio === queue.last()

	private fun setQueue(audios: Collection<Audio>, head: Audio?) {
		originalPlaylist = ArrayList(audios)
		historyStack.clear()
		setupQueues(head)
	}

	fun addToQueue(audios: Collection<Audio>) {
		TODO()
	}

	fun addToPlayNext(audios: Collection<Audio>) {
		playNextQueue.addAll(audios.map(Audio::clone))
	}

	private fun setupQueues(head: Audio? = null, shouldIncludeHead: Boolean = true) {
		if (!randomState) {
			setupDefaultQueue(
					head = head,
					shouldIncludeHead = shouldIncludeHead)
		} else {
			setupShuffledQueue(
					head = head,
					shouldIncludeHead = shouldIncludeHead)
		}
		logPlaylist()
	}

	private fun setupDefaultQueue(head: Audio?, shouldIncludeHead: Boolean) {
		playlist = originalPlaylist

		if (head != null) {
			val headIndex = playlist.indexOf(head)
			val firstPart = if (headIndex > 0) {
				playlist.subList(0, headIndex)
			} else {
				emptyList<Audio>()
			}
			val lastPart = if (headIndex != playlist.size - 1) {
				playlist.subList(headIndex + 1, playlist.size)
			} else {
				emptyList<Audio>()
			}
			queue = ArrayList<Audio>().apply {
				if (shouldIncludeHead) {
					add(head)
				}

				addAll(lastPart)
				addAll(firstPart)
			}
		} else {
			queue = ArrayList(playlist)
		}
	}

	private fun setupShuffledQueue(head: Audio?, shouldIncludeHead: Boolean) {
		val shuffled = originalPlaylist.toMutableList()
		Collections.shuffle(shuffled)
		if (head != null) {
			val index = shuffled.indexOf(head)
			if (index != -1) {
				shuffled.removeAt(index)
			}

			if (shouldIncludeHead) {
				shuffled.add(0, head)
			}
		}
		queue = shuffled
		playlist = queue.toList()
	}

	private fun pollNextAudio(): Audio = when {
		playNextQueue.isNotEmpty() -> {
			playNextQueue.poll()
		}

		else                       -> {
			if (queue.isEmpty()) {
				setupQueues()
			}
			val next = queue[0]
			queue.removeAt(0)
			next
		}
	}

	private fun pollPreviousAudio(): Audio = when {
		historyStack.isNotEmpty() -> {
			historyStack.pop()
		}

		else                      -> {
			logPlaylist()
			val index = playlist.indexOf(audio)
			if (index == 0 || index == -1) {
				playlist.last()
			} else {
				playlist[index - 1]
			}
		}
	}

	/**
	 * Indicates that audio should be played after preparing
	 */
	var shouldPlay = false

	var pauseTime: Int = 0
		private set

	var isReady = false
		private set

	var randomState by observable(false) { property, oldRandomState, randomState ->
		setupQueues(
				head = audio,
				shouldIncludeHead = false)
		historyStack.clear()
		EventBus.post(PlayerRandomChangedEvent(randomState))
	}

	var repeatState by observable(NO_REPEAT) { property, oldValue, newValue ->
		EventBus.post(PlayerRepeatChangedEvent(newValue))
	}

	/**
	 * Handler of UI-Thread for posting events
	 */
	private val uiHandler = Handler(Looper.getMainLooper())

	/**
	 * Observes playing progress of player and posts events about changes
	 */
	private var progressThread: ProgressThread? = null

	init {
		setAudioStreamType(AudioManager.STREAM_MUSIC)
		setOnPreparedListener(this)
		setOnCompletionListener(this)
	}

	fun play(audios: Collection<Audio>, audio: Audio) = if (audio in audios) {
		this.audio = null
		setQueue(audios, audio)
		next()
	} else {
		throw IllegalStateException("Collection must contain given audio!")
	}

	private fun play(audio: Audio) {
		reset()
		stopObserveProgress()
		setOnBufferingUpdateListener(null)

		this.audio = audio
		setDataSource(audio.source)

		shouldPlay = true
		prepareAsync()

		EventBus.post(PlayerPlayEvent(audioPosition, audio))
	}

	fun resume() {
		if (!isReady) {
			shouldPlay = true
		} else {
			seekTo(pauseTime)
			start()
		}

		audio?.let { EventBus.post(PlayerResumeEvent(audioPosition, it)) }
	}

	override fun stop() {
		super.reset()
		audio?.let {
			EventBus.post(PlayerStopEvent(audioPosition, it))
			audio = null
		}
	}

	/**
	 * Pause with flag about stopping foreground notification
	 */
	override fun pause() {
		pause(shouldStopForeground = false)
	}

	fun pause(shouldStopForeground: Boolean) {
		if (!isReady) {
			shouldPlay = false
		} else if (isPlaying) {
			super.pause()
			pauseTime = currentPosition
		}

		audio?.let {
			EventBus.post(PlayerPauseEvent(audioPosition, it, shouldStopForeground))
		}
	}

	fun next() {
		audio?.let { historyStack.push(it) }
		reset()
		play(pollNextAudio())

		logPlaylist()
	}

	fun previous() {
		audio?.let { queue.add(0, it) }
		reset()
		play(pollPreviousAudio())

		logPlaylist()
	}

	override fun seekTo(milliseconds: Int) {
		if (isReady) {
			super.seekTo(milliseconds)
			pauseTime = milliseconds
		}
	}

	fun switchRepeatState(): RepeatState {
		repeatState = when (repeatState) {
			NO_REPEAT  -> ALL_REPEAT
			ALL_REPEAT -> ONE_REPEAT
			ONE_REPEAT -> NO_REPEAT
		}
		return repeatState
	}

	fun switchRandomState() {
		randomState = !randomState
	}

	override fun onCompletion(mp: MediaPlayer) {
		if (repeatState == NO_REPEAT && isLast) {
			stop()
			return
		}

		if (repeatState != ONE_REPEAT) {
			next()
		} else {
			audio?.let { play(it) }
		}

	}

	override fun onPrepared(mp: MediaPlayer) {
		isReady = true
		setOnBufferingUpdateListener(this)

		if (shouldPlay) {
			start()
			startObserveProgress()
		}

		audio?.let {
			EventBus.post(PlayerStartEvent(audioPosition, it))
		}

	}

	override fun reset() {
		super.reset()
		isReady = false
	}

	override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
		notifyBufferingUpdate(percent * duration / 100)
	}

	private fun notifyPlayerProgressChanged() {
		uiHandler.post { EventBus.post(PlayerProgressChangedEvent(currentPosition)) }
	}

	private fun notifyBufferingUpdate(milliseconds: Int) {
		uiHandler.post { EventBus.post(PlayerBufferingUpdateEvent(milliseconds)) }
	}

	private fun startObserveProgress() {
		progressThread = ProgressThread().apply {
			start()
		}
	}

	private fun stopObserveProgress() = progressThread?.let {
		if (!it.isInterrupted) {
			it.interrupt()
		}
	}

	private fun logPlaylist() {
		i(TAG, "Playlist: $playlist")
		i(TAG, "")
		i(TAG, "HistoryStack: Size = ${historyStack.size}")
		i(TAG, "HistoryStack: $historyStack")
		i(TAG, "")
		i(TAG, "Queue: Size = ${queue.size}")
		i(TAG, "Queue: $queue")
		i(TAG, "=================================================")
	}

	private class ProgressThread : Thread() {

		override fun run() = try {
			while (!Thread.interrupted()) {
				if (isPlaying) {
					notifyPlayerProgressChanged()
				}
				Thread.sleep(500)
			}
		} catch (ignore: InterruptedException) {
		}
	}

	enum class RepeatState {
		NO_REPEAT,
		ONE_REPEAT,
		ALL_REPEAT
	}
}
