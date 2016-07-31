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

package com.irateam.vkplayer.controller

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.event.MetadataLoadedEvent
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.models.Metadata
import com.irateam.vkplayer.util.Formatters
import com.irateam.vkplayer.util.getViewById
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ActivityPlayerController : PlayerController {

    val currentTime: TextView
    val remainingTime: TextView
    val position: TextView
    val size: TextView
    val cover: ImageView

    constructor(context: Context, view: View) : super(context, view) {
        currentTime = view.getViewById(R.id.player_panel_current_time)
        remainingTime = view.getViewById(R.id.player_panel_time_remaining)
        position = view.getViewById(R.id.player_panel_count_audio)
        size = view.getViewById(R.id.player_panel_audio_size)
        cover = view.getViewById(R.id.album_art)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMetadataLoaded(e: MetadataLoadedEvent) {
        setMetadata(e.metadata)
    }

    override fun setPlayPause(play: Boolean) {
        super.setPlayPause(play)
        if (play) {
            playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_player_pause_grey_24dp))
        } else {
            playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_player_play_grey_24dp))
        }
    }

    override fun setAudio(index: Int, audio: Audio?) = audio?.let {
        super.setAudio(index, audio)
        clearAudioInfo()

        songName.text = audio.title
        position.text = "#${index + 1}/${player.queue.size}"
        audio.metadata?.let { setMetadata(it) }
    }

    fun clearAudioInfo() {
        size.text = ""
        setProgress(0)
        progress.secondaryProgress = 0
        cover.setImageResource(R.drawable.player_cover)
    }

    fun setMetadata(metadata: Metadata) {
        size.text = Formatters.size(metadata.size)
        metadata.cover?.let { cover.setImageBitmap(it) }
    }

    override fun setProgress(milliseconds: Int) {
        super.setProgress(milliseconds)
        currentTime.text = Formatters.duration(milliseconds)
        val remaining = progress.max - progress.progress
        remainingTime.text = "-${Formatters.duration(remaining)}"
    }
}
