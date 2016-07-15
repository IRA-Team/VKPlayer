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

package com.irateam.vkplayer.controllers

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.player.*
import com.irateam.vkplayer.player.Player.RepeatState.*
import com.melnykov.fab.FloatingActionButton
import org.greenrobot.eventbus.Subscribe

/*
* Class that observe player service for playback events and controls it with ui
*/
open class PlayerController {

    protected val resources: Resources
    protected val player: Player = Player.getInstance()

    /*
    * Views
    */
    val rootView: View

    val songName: TextView
    val author: TextView

    val repeat: ImageView
    val previous: ImageView
    val playPause: ImageView
    val next: ImageView
    val random: ImageView

    val progress: SeekBar
    val fab: FloatingActionButton

    /*
    * Flag that indicates user holds progress bar
    */
    var dragMode: Boolean = false


    /*
    * Constructor accepts 2 parameters:
    * Context for access to resources
    * Root view that contains views for playback controls
    */
    constructor(context: Context, view: View) {
        resources = context.resources

        rootView = view
        fab = rootView.findViewById(R.id.fab) as FloatingActionButton

        songName = rootView.findViewById(R.id.player_panel_song_name) as TextView
        author = rootView.findViewById(R.id.player_panel_author) as TextView

        repeat = rootView.findViewById(R.id.player_panel_repeat) as ImageView
        previous = rootView.findViewById(R.id.player_panel_previous) as ImageView
        playPause = rootView.findViewById(R.id.player_panel_play_pause) as ImageView
        next = rootView.findViewById(R.id.player_panel_next) as ImageView
        random = rootView.findViewById(R.id.player_panel_random) as ImageView

        progress = rootView.findViewById(R.id.progress) as SeekBar
    }

    /*
    * Configuring view events when playerService is set up
    */
    @SuppressWarnings("deprecation")
    open fun initialize() {
        configurePanel()

        setPlayPause(player.isPlaying)

        playPause.setOnClickListener { if (player.isPlaying) player.pause() else player.resume() }
        previous.setOnClickListener { player.previous() }
        next.setOnClickListener { v -> player.next() }

        setRepeatState(player.repeatState)
        repeat.setOnClickListener { setRepeatState(player.repeatState) }

        setRandomState(player.randomState)
        random.setOnClickListener { setRandomState(player.randomState) }

        if (player.isReady && !player.isPlaying) {
            progress.progress = player.pauseTime
        }

        progress.setOnSeekBarChangeListener(ProgressBarChangeListener())
    }

    @Suppress("deprecation")
    open fun setPlayPause(play: Boolean) {
        val drawable = if (play) R.drawable.ic_player_pause_grey_18dp else R.drawable.ic_player_play_grey_18dp
        playPause.setImageDrawable(resources.getDrawable(drawable))
    }

    fun configurePanel() {
        val audio = player.playingAudio
        val index = player.playingAudioIndex

        if (audio != null) {
            rootView.visibility = View.VISIBLE
            setAudio(index, audio)
            setRepeatState(player.repeatState)
            setRandomState(player.randomState)
        }
    }

    fun setFabOnClickListener(listener: View.OnClickListener) {
        fab.setOnClickListener(listener)
    }

    @Subscribe
    open fun onPlayEvent(e: PlayerPlayEvent) {
        val index = e.index
        val audio = e.audio

        setAudio(index, audio)
        setPlayPause(true)
    }

    @Subscribe
    open fun onPauseEvent(e: PlayerPauseEvent) {
        setPlayPause(false)
    }

    @Subscribe
    open fun onResumeEvent(e: PlayerResumeEvent) {
        setPlayPause(true)
    }

    @Subscribe
    open fun onStopEvent(e: PlayerStopEvent) {
        rootView.visibility = View.GONE
    }

    @Subscribe
    open fun onProgressChangedEvent(e: PlayerProgressChangedEvent) {
        setProgress(e.milliseconds)
    }

    @Subscribe
    open fun onBufferingUpdate(e: PlayerBufferingUpdateEvent) {
        progress.secondaryProgress = e.milliseconds
    }

    open fun setAudio(index: Int, audio: Audio?) {
        if (audio != null) {
            val position = (index + 1).toString()

            if (rootView.visibility != View.VISIBLE) {
                rootView.visibility = View.VISIBLE
            }

            songName.text = position + ". " + audio.title
            author.text = audio.artist
            progress.max = audio.duration * 1000
            progress.progress = 0
            progress.secondaryProgress = 0
        }
    }

    open fun setProgress(milliseconds: Int) {
        if (!dragMode) {
            progress.progress = milliseconds
        }
    }

    @Suppress("deprecation")
    fun setRepeatState(repeatState: Player.RepeatState) {
        when (repeatState) {
            NO_REPEAT -> repeat.setImageDrawable(resources.getDrawable(R.drawable.ic_player_repeat_light_grey_18dp))
            ALL_REPEAT -> repeat.setImageDrawable(resources.getDrawable(R.drawable.ic_player_repeat_all_light_grey_18dp))
            ONE_REPEAT -> repeat.setImageDrawable(resources.getDrawable(R.drawable.ic_player_repeat_one_light_grey_18dp))
        }
    }

    @Suppress("deprecation")
    fun setRandomState(randomState: Boolean) {
        if (randomState) {
            random.setImageDrawable(resources.getDrawable(R.drawable.ic_player_random_on_light_grey_18dp))
        } else {
            random.setImageDrawable(resources.getDrawable(R.drawable.ic_player_random_light_grey_18dp))
        }
    }

    private inner class ProgressBarChangeListener : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            dragMode = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            dragMode = false
            player.seekTo(progress.progress)
        }
    }
}
