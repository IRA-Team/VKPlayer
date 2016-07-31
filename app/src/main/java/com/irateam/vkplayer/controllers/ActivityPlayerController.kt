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
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.models.Metadata
import com.irateam.vkplayer.player.PlayerProgressChangedEvent
import com.irateam.vkplayer.player.PlayerStartEvent
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class ActivityPlayerController : PlayerController {


    val currentTime: TextView
    val timeToFinish: TextView
    val numberAudio: TextView
    val sizeAudio: TextView
    val albumArt: ImageView

    constructor(context: Context, view: View) : super(context, view) {
        currentTime = view.findViewById(R.id.player_panel_current_time) as TextView
        timeToFinish = view.findViewById(R.id.player_panel_time_remaining) as TextView
        numberAudio = view.findViewById(R.id.player_panel_count_audio) as TextView
        sizeAudio = view.findViewById(R.id.player_panel_audio_size) as TextView
        albumArt = view.findViewById(R.id.album_art) as ImageView
    }

    @SuppressWarnings("deprecation")
    override fun initialize() {
        super.initialize()
        //TODO: playerService.getPlayingAudio().getAudioInfo().getWithListener(this)
    }

    @Subscribe
    fun onStartEvent(e: PlayerStartEvent) {
        //TODO: audio.getAudioInfo().getWithListener(this);
    }

    override fun setPlayPause(play: Boolean) {
        super.setPlayPause(play)
        if (play)
            playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_player_pause_grey_24dp))
        else
            playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_player_play_grey_24dp))
    }

    override fun setAudio(index: Int, audio: Audio?) {
        super.setAudio(index, audio)
        clearAudioInfo()

        songName.text = audio!!.title
        numberAudio.text = "#" + (index + 1) + "/" + player.queue.size
    }

    fun clearAudioInfo() {
        sizeAudio.text = ""
        setProgress(0)
        progress.secondaryProgress = 0
        albumArt.setImageResource(R.drawable.player_cover)
    }

    fun setAudioInfo(info: Metadata) {
        sizeAudio.text = String.format("%.1f", info.size.toDouble() / 1024.toDouble() / 1024.toDouble()) + "Mb" + " " + info.bitrate
        if (info.cover != null) {
            albumArt.setImageBitmap(info.cover)
        }
    }

    //TODO: this method looks like shit
    override fun setProgress(milliseconds: Int) {
        super.setProgress(milliseconds)
        currentTime.text = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(progress.progress.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(progress.progress.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress.progress.toLong())))
        val timeRemaining = progress.max - progress.progress
        timeToFinish.text = "-" + String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeRemaining.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(timeRemaining.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeRemaining.toLong())))

    }

    fun OnComplete(metadata: Metadata) {
        setAudioInfo(metadata)
    }

    fun OnError() {

    }
}
