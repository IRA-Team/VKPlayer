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
import kotlin.properties.Delegates.observable

object Player : MediaPlayer(),
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnBufferingUpdateListener {

    val TAG: String = Player::class.java.name

    private val playlistManager: PlaylistManager = PlaylistManager()

    /**
     * Delegates from PlaylistManager
     */
    val originalPlaylist: List<Audio>
        get() = playlistManager.originalPlaylist

    val playlist: List<Audio>
        get() = playlistManager.playlist

    val queueSize: Int
        get() = playlistManager.queueSize

    val audio: Audio?
        get() = playlistManager.audio

    val audioPosition: Int
        get() = playlistManager.audioPosition

    fun addToPlayNext(audios: Collection<Audio>) {
        playlistManager.addToPlayNext(audios)
    }

    fun addToQueue(audios: Collection<Audio>) {
        playlistManager.addToQueue(audios)
    }

    /**
     * Indicates that audio should be played after preparing
     */
    var shouldPlay = false
        private set

    var pauseTime: Int = 0
        private set

    var isReady = false
        private set

    var randomState by observable(false) { property, oldRandomState, randomState ->
        playlistManager.random = randomState
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
        playlistManager.setQueue(
                audios = audios,
                head = audio,
                random = randomState)
        next()
    } else {
        throw IllegalStateException("Collection must contain given audio!")
    }

    private fun play(audio: Audio) {
        reset()
        stopObserveProgress()
        setOnBufferingUpdateListener(null)

        setDataSource(audio.source)

        shouldPlay = true
        prepareAsync()

        EventBus.post(PlayerPlayEvent(playlistManager.audioPosition, audio))
    }

    fun resume() {
        if (!isReady) {
            shouldPlay = true
        } else {
            seekTo(pauseTime)
            start()
        }

        audio?.let { EventBus.post(PlayerResumeEvent(playlistManager.audioPosition, it)) }
    }

    override fun stop() {
        super.reset()
        audio?.let {
            EventBus.post(PlayerStopEvent(playlistManager.audioPosition, it))
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
            EventBus.post(PlayerPauseEvent(playlistManager.audioPosition, it, shouldStopForeground))
        }
    }

    fun next() {
        reset()
        play(playlistManager.pollNextAudio())
    }

    fun previous() {
        reset()
        play(playlistManager.pollPreviousAudio())
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
        if (repeatState == NO_REPEAT && playlistManager.queueSize == 0) {
            stop()
            playlistManager.reset()
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
            EventBus.post(PlayerStartEvent(playlistManager.audioPosition, it))
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

    interface PlaylistManager {
        var random: Boolean

        val originalPlaylist: List<Audio>
        val playlist: List<Audio>
        val queueSize: Int
        val audioPosition: Int
        val audio: Audio?

        fun setQueue(audios: Collection<Audio>, head: Audio?, random: Boolean)
        fun addToQueue(audios: Collection<Audio>)
        fun addToPlayNext(audios: Collection<Audio>)

        fun reset()
        fun pollNextAudio(): Audio
        fun pollPreviousAudio(): Audio
    }
}
