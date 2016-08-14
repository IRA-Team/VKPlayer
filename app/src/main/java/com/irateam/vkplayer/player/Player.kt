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
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.player.Player.RepeatState.*
import com.irateam.vkplayer.util.EventBus
import java.util.*
import kotlin.properties.Delegates.observable

object Player : MediaPlayer(),
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnBufferingUpdateListener {

    /**
     * Queues
     */
    private val mainPart = ArrayList<Audio>()
    private val nextPart = ArrayList<Audio>()

    private var playingQueue: List<Audio> = emptyList<Audio>()
    private var shuffledQueue: List<Audio>? = null

    val queue: List<Audio>
        get() = if (randomState) {
            shuffledQueue ?: emptyList<Audio>()
        } else {
            playingQueue
        }

    var audio: Audio? = null
        private set

    val queueSize: Int
        get() = queue.size

    val audioIndex: Int
        get() = queue.indexOf(audio)

    val audioPosition: Int
        get() = playingQueue.indexOf(audio)

    val isFirst: Boolean
        get() = audio === queue.first()

    val isLast: Boolean
        get() = audio === queue.last()

    fun setQueue(audios: Collection<Audio>) {
        mainPart.clear()
        mainPart.addAll(audios)
        setupQueues()
    }

    fun addToQueue(audios: Collection<Audio>) {
        mainPart.addAll(0, audios.map { it.clone() })
        setupQueues()
    }

    private fun setupQueues() {
        setupPlayingQueue()
        if (randomState) {
            setupShuffledQueue()
        }
    }

    private fun setupPlayingQueue() {
        val playingQueue = ArrayList<Audio>()
        playingQueue.addAll(nextPart)
        playingQueue.addAll(mainPart)
        this.playingQueue = playingQueue
    }

    private fun setupShuffledQueue(randomState: Boolean = true,
                                   head: Audio? = audio) {

        shuffledQueue = if (randomState) {
            val shuffledPart = ArrayList<Audio>(mainPart)
            Collections.shuffle(shuffledPart)
            head?.let {
                val index = shuffledPart.indexOf(it)
                if (index != -1) {
                    shuffledPart.removeAt(index)
                    shuffledPart.add(0, head)
                }
            }

            val shuffledQueue = ArrayList<Audio>()
            shuffledQueue.addAll(nextPart)
            shuffledQueue.addAll(shuffledPart)
            shuffledQueue
        } else {
            null
        }
    }

    /**
     * Player state
     */
    var pauseTime: Int = 0
        private set

    var isReady = false
        private set

    var randomState by observable(false) { property, oldValue, newValue ->
        setupShuffledQueue(randomState = newValue)
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

    fun play(audios: Collection<Audio>, audio: Audio) {
        setQueue(audios)
        if (audio in playingQueue) {
            play(audio)
        } else {
            throw IllegalStateException("Collection must contain given audio!")
        }
    }


    private fun play(audio: Audio) {
        reset()
        stopProgress()
        setOnBufferingUpdateListener(null)

        val source = if (audio.isCached) audio.cachePath else audio.url
        setDataSource(source)
        prepareAsync()

        this.audio = audio
        EventBus.post(PlayerPlayEvent(audioPosition, audio))
    }

    fun resume() = audio?.let {
        if (isReady) {
            seekTo(pauseTime)
            start()
            EventBus.post(PlayerResumeEvent(audioPosition, it))
        }
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
        if (isReady && isPlaying) {
            super.pause()
            pauseTime = currentPosition
        }

        audio?.let {
            EventBus.post(PlayerPauseEvent(audioPosition, it, shouldStopForeground))
        }
    }

    fun next() {
        if (queue.isEmpty()) {
            stop()
            return
        }

        val nextIndex = if (!isLast) {
            audioIndex + 1
        } else {
            0
        }

        reset()
        play(queue[nextIndex])
    }

    fun previous() {
        if (queue.isEmpty()) {
            stop()
            return
        }

        val previousIndex = if (isFirst) {
            queue.size - 1
        } else if (audioIndex == -1) {
            0
        } else {
            audioIndex - 1
        }

        reset()
        play(queue[previousIndex])
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
        start()
        startProgress()
        setOnBufferingUpdateListener(this)

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
        uiHandler.post {
            val currentPosition = currentPosition
            EventBus.post(PlayerProgressChangedEvent(currentPosition))
        }
    }

    private fun notifyBufferingUpdate(milliseconds: Int) {
        uiHandler.post { EventBus.post(PlayerBufferingUpdateEvent(milliseconds)) }
    }

    fun startProgress() {
        progressThread = ProgressThread().apply {
            start()
        }
    }

    fun stopProgress() = progressThread?.let {
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
