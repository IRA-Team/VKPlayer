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
import android.util.Log
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.player.Player.RepeatState.*
import com.irateam.vkplayer.util.EventBus
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.properties.Delegates.observable

object Player : MediaPlayer(),
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnBufferingUpdateListener {

    val TAG = Player.javaClass.name

    /**
     * Queues
     */
    private var originalPlaylist = ArrayList<Audio>()
    var playlist = emptyList<Audio>()
        private set

    private var queue = ArrayList<Audio>()
    private val playNextQueue = ConcurrentLinkedQueue<Audio>()
    private val historyStack = Stack<Audio>()

    var audio: Audio? = null
        private set

    val queueSize: Int
        get() = queue.size

    val audioIndex: Int
        get() = queue.indexOf(audio)

    val audioPosition: Int
        get() = originalPlaylist.indexOf(audio)

    val isFirst: Boolean
        get() = audio === queue.first()

    val isLast: Boolean
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
        playNextQueue.addAll(audios.map { it.clone() })
    }

    private fun setupQueues(head: Audio? = null) {
        if (!randomState) {
            setupPlayingQueue(head)
        } else {
            setupShuffledQueue(head)
        }
    }

    private fun setupPlayingQueue(head: Audio?) {
        playlist = originalPlaylist.toList()

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
                add(head)
                addAll(lastPart)
                addAll(firstPart)
            }
        } else {
            queue = ArrayList(playlist)
        }
    }

    private fun setupShuffledQueue(head: Audio?) {
        val shuffled = ArrayList<Audio>(originalPlaylist)
        Collections.shuffle(shuffled)
        if (head != null) {
            val index = shuffled.indexOf(head)
            if (index != -1) {
                shuffled.removeAt(index)
            }
            shuffled.add(0, head)
        }
        queue = shuffled
        playlist = queue.toList()
    }

    private fun pollNextAudio(): Audio = when {
        playNextQueue.isNotEmpty() -> {
            playNextQueue.poll()
        }

        else -> {
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

        else -> {
            val index = playlist.indexOf(audio)
            if (index == 0) {
                playlist.last()
            } else {
                playlist[index - 1]
            }
        }
    }

    /**
     * Player state
     */
    var shouldPlay = false

    var pauseTime: Int = 0
        private set

    var isReady = false
        private set

    var randomState by observable(false) { property, oldValue, newValue ->
        setupQueues(audio)
        EventBus.post(PlayerRandomChangedEvent(newValue))
    }

    var repeatState by observable(NO_REPEAT) { property, oldValue, newValue ->
        EventBus.post(PlayerRepeatChangedEvent(newValue))
    }

    /**
     * Other
     */
    private val uiHandler = Handler(Looper.getMainLooper())
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
        val source = if (audio.isCached) audio.cachePath else audio.url
        setDataSource(source)

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

    override fun pause() {
        pause(false)
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

        Log.v(TAG, "HistoryStack: Size = ${historyStack.size}")
        Log.v(TAG, "HistoryStack: $historyStack")
        Log.v(TAG, "Queue: Size = ${queue.size}")
        Log.v(TAG, "Queue: $queue")
    }

    fun previous() {
        audio?.let { queue.add(0, it) }
        reset()
        play(pollPreviousAudio())

        Log.v(TAG, "HistoryStack: Size = ${historyStack.size}")
        Log.v(TAG, "HistoryStack: $historyStack")
        Log.v(TAG, "Queue: Size = ${queue.size}")
        Log.v(TAG, "Queue: $queue")
    }

    override fun seekTo(milliseconds: Int) {
        if (isReady) {
            super.seekTo(milliseconds)
            pauseTime = milliseconds
        }
    }

    fun switchRepeatState(): RepeatState {
        repeatState = when (repeatState) {
            NO_REPEAT -> ALL_REPEAT
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
