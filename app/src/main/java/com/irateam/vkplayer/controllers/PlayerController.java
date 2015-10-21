package com.irateam.vkplayer.controllers;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.player.Player;
import com.irateam.vkplayer.services.PlayerService;
import com.melnykov.fab.FloatingActionButton;

public class PlayerController implements Player.PlayerEventListener, Player.PlayerProgressListener {

    public View rootView;
    public FloatingActionButton fab;

    public TextView songName;
    public TextView author;

    public ImageView repeat;
    public ImageView previous;
    public ImageView playPause;
    public ImageView next;
    public ImageView random;

    public SeekBar progress;
    private boolean dragMode;

    protected Context context;
    protected Resources resources;

    private LinearLayout headerLayout;
    protected PlayerService playerService;

    public PlayerController(Context context, View view) {
        this.context = context;
        resources = context.getResources();

        rootView = view;
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        
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
        this.playerService = playerService;
        configurePanel(playerService);

        setPlayPause(playerService.isPlaying());
        playPause.setOnClickListener((v -> {
            if (playerService.isPlaying())
                playerService.pause();
            else
                playerService.resume();
        }));

        previous.setOnClickListener((v) ->
                playerService.previous());

        next.setOnClickListener((v) ->
                playerService.next());

        setRepeatState(playerService.getRepeatState());
        repeat.setOnClickListener((v) ->
                setRepeatState(playerService.switchRepeatState()));

        setRandomState(playerService.getRandomState());
        random.setOnClickListener((v) ->
                setRandomState(playerService.switchRandomState()));

        if (playerService.isReady() && !playerService.isPlaying()) {
            onProgressChanged(playerService.getPauseTime());
        }
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

    public void setPlayPause(boolean play) {
        if (play)
            playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_player_pause_grey_18dp));
        else
            playPause.setImageDrawable(resources.getDrawable(R.drawable.ic_player_play_grey_18dp));
    }

    public void configurePanel(PlayerService playerService) {
        playerService.addPlayerProgressListener(this);
        Audio audio = playerService.getPlayingAudio();
        if (audio != null) {
            rootView.setVisibility(View.VISIBLE);
            setAudio(playerService.getPlayingAudioIndex(), audio);
            setRepeatState(playerService.getRepeatState());
            setRandomState(playerService.getRandomState());
        }
    }

    public void setFabOnClickListener(View.OnClickListener listener) {
        fab.setOnClickListener(listener);
    }

    @Override
    public void onEvent(int position, Audio audio, Player.PlayerEvent event) {
        switch (event) {
            case PLAY:
                setAudio(position, audio);
                setPlayPause(true);
                break;
            case PAUSE:
                setPlayPause(false);
                break;
            case RESUME:
                setPlayPause(true);
                break;
            case STOP:
                rootView.setVisibility(View.GONE);
                break;
        }
    }

    public void setAudio(int position, Audio audio) {
        if (audio != null) {
            if (rootView.getVisibility() != View.VISIBLE) {
                rootView.setVisibility(View.VISIBLE);
            }
            songName.setText(position + 1 + ". " + audio.title);
            author.setText(audio.artist);
            progress.setMax(audio.duration * 1000);
            progress.setProgress(0);
            progress.setSecondaryProgress(0);
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
    public void setRandomState(boolean randomState) {
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

    @Override
    public void onBufferingUpdate(int milliseconds) {
        progress.setSecondaryProgress(milliseconds);
    }
}
