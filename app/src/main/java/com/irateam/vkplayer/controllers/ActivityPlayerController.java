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

package com.irateam.vkplayer.controllers;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.models.AudioInfo;
import com.irateam.vkplayer.player.Player;
import com.irateam.vkplayer.services.PlayerService;

import java.util.concurrent.TimeUnit;

public class ActivityPlayerController extends PlayerController implements AudioInfo.AudioInfoListener {

    public TextView currentTime;
    public TextView timeToFinish;
    public TextView numberAudio;
    public TextView sizeAudio;
    public ImageView albumArt;

    private Resources resources;

    public ActivityPlayerController(Context context, View view) {
        super(context, view);

        resources = context.getResources();

        currentTime = (TextView) view.findViewById(R.id.player_panel_current_time);
        timeToFinish = (TextView) view.findViewById(R.id.player_panel_time_remaining);
        numberAudio = (TextView) view.findViewById(R.id.player_panel_count_audio);
        sizeAudio = (TextView) view.findViewById(R.id.player_panel_audio_size);
        albumArt = (ImageView) view.findViewById(R.id.album_art);
    }

    @SuppressWarnings("deprecation")
    public void setPlayerService(final PlayerService playerService) {
        super.setPlayerService(playerService);
        playPause.setOnClickListener((v) -> {
            if (playerService.isPlaying()) {
                playerService.pause();
            } else {
                playerService.resume();
            }
        });
        playerService.getPlayingAudio().getAudioInfo().getWithListener(this);
    }

    @Override
    public void onEvent(int position, Audio audio, Player.PlayerEvent event) {
        super.onEvent(position, audio, event);
        switch (event) {
            case START:
                audio.getAudioInfo().getWithListener(this);
                break;
        }
    }

    public void setPlayPause(boolean play) {
        super.setPlayPause(play);
        if (play)
            playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_player_pause_grey_24dp));
        else
            playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_player_play_grey_24dp));
    }

    public void setAudio(int position, Audio audio) {
        super.setAudio(position, audio);
        clearAudioInfo();

        songName.setText(audio.getTitle());
        numberAudio.setText("#" + (position + 1) + "/" + playerService.getPlaylist().size());
    }

    public void clearAudioInfo() {
        sizeAudio.setText("");
        onProgressChanged(0);
        progress.setSecondaryProgress(0);
        albumArt.setImageResource(R.drawable.player_cover);
    }

    public void setAudioInfo(AudioInfo info) {
        sizeAudio.setText(String.format("%.1f", info.size / (double) 1024 / (double) 1024) + "Mb");
        sizeAudio.setText(sizeAudio.getText() + " " + info.bitrate);
        if (info.cover != null) {
            albumArt.setImageBitmap(info.cover);
        }
    }

    @Override
    public void onProgressChanged(int milliseconds) {
        super.onProgressChanged(milliseconds);
        currentTime.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(progress.getProgress()),
                TimeUnit.MILLISECONDS.toSeconds(progress.getProgress()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress.getProgress()))
        ));
        int timeRemaining = progress.getMax() - progress.getProgress();
        timeToFinish.setText("-" + String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeRemaining),
                TimeUnit.MILLISECONDS.toSeconds(timeRemaining) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeRemaining))
        ));
    }

    @Override
    public void OnComplete(AudioInfo audioInfo) {
        setAudioInfo(audioInfo);
    }

    @Override
    public void OnError() {

    }
}
