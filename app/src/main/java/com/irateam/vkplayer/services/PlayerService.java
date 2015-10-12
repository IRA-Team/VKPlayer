package com.irateam.vkplayer.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.irateam.vkplayer.notifications.PlayerNotification;
import com.irateam.vkplayer.player.Player;
import com.vk.sdk.api.model.VKApiAudio;

import java.util.List;

public class PlayerService extends Service implements Player.PlayerEventListener {

    public static final String PREVIOUS = "playerService.PREVIOUS";
    public static final String PAUSE = "playerService.PAUSE";
    public static final String RESUME = "playerService.RESUME";
    public static final String NEXT = "playerService.NEXT";

    private Player player = new Player();
    private Binder binder = new PlayerBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        player.addPlayerEventListener(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case PREVIOUS:
                    previous();
                    break;
                case PAUSE:
                    pause();
                    break;
                case RESUME:
                    resume();
                    break;
                case NEXT:
                    next();
                    break;
            }
        }
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        player.removePlayerEventListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class PlayerBinder extends Binder {
        public PlayerService getPlayerService() {
            return PlayerService.this;
        }
    }

    //Player methods
    public void setPlaylist(List<VKApiAudio> list) {
        player.setList(list);
    }

    public List<VKApiAudio> getPlaylist() {
        return player.getList();
    }

    public void play(int index) {
        player.play(index);
    }

    public void resume() {
        player.resume();
    }

    public void pause() {
        player.pause();
    }

    public void stop() {
        player.stop();
    }

    public void next() {
        player.next();
    }

    public void previous() {
        player.previous();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public VKApiAudio getPlayingAudio() {
        return player.getPlayingAudio();
    }

    public Integer getPlayingAudioIndex() {
        return player.getPlayingAudioIndex();
    }

    public Player.RepeatState switchRepeatState() {
        return player.switchRepeatState();
    }

    public Player.RepeatState getRepeatState() {
        return player.getRepeatState();
    }

    public boolean switchRandomState() {
        return player.switchRandomState();
    }

    public boolean getRandomState() {
        return player.getRandomState();
    }

    public void addPlayerEventListener(Player.PlayerEventListener listener) {
        player.addPlayerEventListener(listener);
    }

    public void removePlayerEventListener(Player.PlayerEventListener listener) {
        player.removePlayerEventListener(listener);
    }

    public void addPlayerProgressListener(Player.PlayerProgressListener listener) {
        player.addPlayerProgressListener(listener);
    }

    public void removePlayerProgressListener(Player.PlayerProgressListener listener) {
        player.removePlayerProgressListener(listener);
    }

    public void seekTo(int milliseconds) {
        player.seekTo(milliseconds);
    }

    //Player callbacks
    @Override
    public void onEvent(int position, VKApiAudio audio, Player.PlayerEvent event) {
        switch (event) {
            case PLAY:
                startForeground(PlayerNotification.ID, PlayerNotification.create(this, position, audio, event));
                break;
            case PAUSE:
                PlayerNotification.update(this, position, audio, Player.PlayerEvent.PAUSE);
                break;
            case RESUME:
                PlayerNotification.update(this, position, audio, Player.PlayerEvent.RESUME);
                break;
            case STOP:
                stopForeground(true);
                break;
        }
    }
}