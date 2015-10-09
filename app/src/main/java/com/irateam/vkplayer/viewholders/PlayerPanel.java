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
import com.irateam.vkplayer.services.PlayerService;
import com.vk.sdk.api.model.VKApiAudio;

public class PlayerPanel implements Player.PlayerEventListener, Player.PlayerProgressListener {

    public View rootView;
    public TextView songName;
    public TextView author;

    public ImageView repeat;
    public ImageView previous;
    public ImageView playPause;
    public ImageView next;
    public ImageView random;

    public SeekBar progress;
    private boolean dragMode;

    private Context context;
    private Resources resources;

    public PlayerPanel(Context context, View view) {
        this.context = context;
        resources = context.getResources();

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

    @SuppressWarnings("deprecation")
    public void setPlayerService(final PlayerService playerService) {
        final Resources resources = context.getResources();
        configurePanel(playerService);

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerService.isPlaying()) {
                    playerService.pause();
                    playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_player_play_grey_18dp));
                } else {
                    playerService.resume();
                    playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_player_pause_grey_18dp));
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerService.previous();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerService.next();
            }
        });

        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRepeatState(playerService.switchRepeatState());
            }
        });

        random.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRandomState(playerService.switchRandomState());
            }
        });

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                dragMode = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                dragMode = false;
                playerService.seekTo(progress.getProgress());
            }
        });
    }

    private void configurePanel(PlayerService playerService) {
        playerService.addPlayerProgressListener(this);
        VKApiAudio audio = playerService.getPlayingAudio();
        if (audio != null) {
            rootView.setVisibility(View.VISIBLE);
            setAudio(playerService.getPlayingAudioIndex(), audio);
            setRepeatState(playerService.getRepeatState());
            setRandomState(playerService.getRandomState());
        }
    }

    @Override
    public void onEvent(int position, VKApiAudio audio, Player.PlayerEvent event) {
        switch (event) {
            case PLAY:
                setAudio(position, audio);
                break;
        }
    }

    public void setAudio(int position, VKApiAudio audio) {
        if (audio != null) {
            if (rootView.getVisibility() != View.VISIBLE) {
                rootView.setVisibility(View.VISIBLE);
            }
            songName.setText(position + 1 + ". " + audio.title);
            author.setText(audio.artist);
            progress.setMax(audio.duration * 1000);
            progress.setProgress(0);
        }
    }

    @SuppressWarnings("deprecation")
    public void setRepeatState(Player.RepeatState repeatState) {
        switch (repeatState) {
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

    @SuppressWarnings("deprecation")
    private void setRandomState(boolean randomState) {
        if (randomState) {
            random.setImageDrawable(resources.getDrawable(R.drawable.ic_player_random_on_light_grey_18dp));
        } else {
            random.setImageDrawable(resources.getDrawable(R.drawable.ic_player_random_light_grey_18dp));
        }
    }

    @Override
    public void onProgressChanged(int milliseconds) {
        if (!dragMode) {
            progress.setProgress(milliseconds);
        }
    }
}
