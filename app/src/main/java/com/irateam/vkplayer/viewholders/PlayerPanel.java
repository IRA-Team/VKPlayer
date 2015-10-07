package com.irateam.vkplayer.viewholders;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.player.Player;
import com.vk.sdk.api.model.VKApiAudio;

public class PlayerPanel {

    public View rootView;
    public TextView songName;
    public TextView author;

    public ImageView repeat;
    public ImageView previous;
    public ImageView playPause;
    public ImageView next;
    public ImageView random;

    public SeekBar progress;

    public PlayerPanel(View view) {
        rootView = view;
        songName = (TextView) view.findViewById(R.id.player_panel_song_name);
        author = (TextView) view.findViewById(R.id.player_panel_author);

        repeat = (ImageView) view.findViewById(R.id.player_panel_repeat);
        previous = (ImageView) view.findViewById(R.id.player_panel_previous);
        playPause = (ImageView) view.findViewById(R.id.player_panel_play_pause);
        next = (ImageView) view.findViewById(R.id.player_panel_next);
        random = (ImageView) view.findViewById(R.id.player_panel_random);

        progress = (SeekBar) view.findViewById(R.id.progress);
    }

    public void setAudio(int position, VKApiAudio audio) {
        songName.setText(position + 1 + ". " + audio.title);
        author.setText(audio.artist);
    }

    @SuppressWarnings("deprecation")
    public void setPlayer(final Context context, final Player player) {
        final Resources resources = context.getResources();

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.pause();
                    playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_player_play_grey_18dp));
                } else {
                    player.play();
                    playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_player_pause_grey_18dp));
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.previous();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.next();
            }
        });

        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (player.switchRepeatState()) {
                    case NO_REPEAT:
                        repeat.setImageDrawable(resources.getDrawable(R.drawable.ic_player_repeat_light_grey_18dp));
                        break;
                    case ALL_REPEAT:
                        repeat.setImageDrawable(resources.getDrawable(R.drawable.ic_player_repeat_all_light_grey_18dp));
                        break;
                    case ONE_REPEAT:
                        repeat.setImageDrawable(resources.getDrawable(R.drawable.ic_player_repeat_one_light_grey_18dp));
                        break;
                }
            }
        });

        random.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.switchRandomState()) {
                    random.setImageDrawable(resources.getDrawable(R.drawable.ic_player_random_on_light_grey_18dp));
                } else {
                    random.setImageDrawable(resources.getDrawable(R.drawable.ic_player_random_light_grey_18dp));
                }
            }
        });

        player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                progress.setSecondaryProgress(percent);
            }
        });

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
}
