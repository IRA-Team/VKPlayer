package com.irateam.vkplayer.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import com.vk.sdk.api.model.VKApiAudio;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import static com.irateam.vkplayer.player.Player.RepeatState.ALL_REPEAT;
import static com.irateam.vkplayer.player.Player.RepeatState.NO_REPEAT;
import static com.irateam.vkplayer.player.Player.RepeatState.ONE_REPEAT;

public class Player extends MediaPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {

    public static final String proxyURL = "http://localhost:8080/";

    private static Player instance;
    private int pauseTime;

    public synchronized static Player getInstance() {
        if (instance == null) {
            instance = new Player();
        }
        return instance;
    }

    public Player() {
        super();
        setAudioStreamType(AudioManager.STREAM_MUSIC);
        setOnPreparedListener(this);
        setOnCompletionListener(this);
        startProgress();
        setOnBufferingUpdateListener(this);
    }

    private List<VKApiAudio> list;
    private RepeatState repeatState = NO_REPEAT;

    private boolean randomState = false;
    private Stack<VKApiAudio> randomStack = new Stack<>();
    private Random random = new Random();

    private VKApiAudio playingAudio;

    public VKApiAudio getAudio(int index) {
        return list.get(index);
    }

    public VKApiAudio getPlayingAudio() {
        return playingAudio;
    }

    public Integer getPlayingAudioIndex() {
        return playingAudio != null ? list.indexOf(playingAudio) : null;
    }

    public List<VKApiAudio> getList() {
        return list;
    }

    public void setList(List<VKApiAudio> list) {
        this.list = list;
    }

    public void play(int index) {
        playingAudio = list.get(index);
        try {
            reset();
            setDataSource(playingAudio.url);
            prepareAsync();
            notifyPlayerEvent(index, playingAudio, PlayerEvent.PLAY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        if (playingAudio != null) {
            seekTo(pauseTime);
            start();
            notifyPlayerEvent(getPlayingAudioIndex(), playingAudio, PlayerEvent.RESUME);
        }
    }

    public void stop() {
        if (isPlaying() && playingAudio != null) {
            super.stop();
            notifyPlayerEvent(getPlayingAudioIndex(), playingAudio, PlayerEvent.STOP);
            playingAudio = null;
        }
    }

    public void pause() {
        if (isPlaying()) {
            super.pause();
            pauseTime = getCurrentPosition();
            notifyPlayerEvent(getPlayingAudioIndex(), playingAudio, PlayerEvent.PAUSE);
        }
    }

    public void next() {
        int nextIndex;
        if (randomState) {
            nextIndex = random.nextInt(list.size());
            randomStack.push(playingAudio);
        } else {
            nextIndex = getPlayingAudioIndex() + 1;
            if (list.size() == nextIndex) {
                nextIndex = 0;
            }
        }
        reset();
        play(nextIndex);
    }

    public void previous() {
        int previousIndex;
        if (randomState && !randomStack.empty()) {
            previousIndex = list.indexOf(randomStack.pop());
        } else {
            previousIndex = list.indexOf(playingAudio) - 1;
            if (previousIndex == -1) {
                previousIndex = list.size() - 1;
            }
        }
        reset();
        play(previousIndex);
    }

    public RepeatState getRepeatState() {
        return repeatState;
    }

    public RepeatState switchRepeatState() {
        switch (repeatState) {
            case NO_REPEAT:
                repeatState = ALL_REPEAT;
                break;
            case ALL_REPEAT:
                repeatState = ONE_REPEAT;
                break;
            case ONE_REPEAT:
                repeatState = NO_REPEAT;
                break;
        }
        return repeatState;
    }

    public void setRepeatState(RepeatState repeatState) {
        this.repeatState = repeatState;
    }

    public boolean getRandomState() {
        return randomState;
    }

    public boolean switchRandomState() {
        randomState = !randomState;
        if (randomState) {
            randomStack = new Stack<>();
        }
        return randomState;
    }

    public void setRandomState(boolean randomState) {
        this.randomState = randomState;
        if (randomState) {
            randomStack = new Stack<>();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (repeatState == NO_REPEAT && playingAudio == list.get(list.size() - 1)) {
            notifyPlayerEvent(getPlayingAudioIndex(), playingAudio, PlayerEvent.STOP);
            stop();
            return;
        }

        if (repeatState != ONE_REPEAT) {
            if (randomState) {
                randomStack.push(playingAudio);
            }
            next();
        } else {
            play(getPlayingAudioIndex());
        }

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        start();
    }

    //Listeners
    private List<WeakReference<PlayerEventListener>> listeners = new ArrayList<>();

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        notifyBufferingUpdate(percent * getDuration() / 100);
    }

    public interface PlayerEventListener {
        void onEvent(int position, VKApiAudio audio, PlayerEvent event);
    }

    public void addPlayerEventListener(PlayerEventListener listener) {
        listeners.add(new WeakReference<>(listener));
    }

    public void removePlayerEventListener(PlayerEventListener listener) {
        listeners.remove(listener);
    }

    private void notifyPlayerEvent(int position, VKApiAudio audio, PlayerEvent event) {
        for (WeakReference<PlayerEventListener> l : listeners) {
            PlayerEventListener listener = l.get();
            if (listener != null) {
                listener.onEvent(position, audio, event);
            }
        }
    }

    public enum RepeatState {
        NO_REPEAT,
        ONE_REPEAT,
        ALL_REPEAT
    }

    public enum PlayerEvent {
        PLAY,
        PAUSE,
        RESUME,
        STOP
    }

    //Progress Listener
    public void startProgress() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (isPlaying()) {
                            notifyPlayerProgressChanged();
                        }
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private List<WeakReference<PlayerProgressListener>> progressListeners = new ArrayList<>();

    public interface PlayerProgressListener {
        void onProgressChanged(int milliseconds);

        void onBufferingUpdate(int milliseconds);
    }

    public void addPlayerProgressListener(PlayerProgressListener listener) {
        progressListeners.add(new WeakReference<>(listener));
    }

    public void removePlayerProgressListener(PlayerProgressListener listener) {
        progressListeners.remove(listener);
    }

    private void notifyPlayerProgressChanged() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                for (WeakReference<PlayerProgressListener> l : progressListeners) {
                    PlayerProgressListener listener = l.get();
                    if (listener != null) {
                        listener.onProgressChanged(getCurrentPosition());
                    }
                }
            }
        });
    }

    private void notifyBufferingUpdate(int milliseconds) {
        for (WeakReference<PlayerProgressListener> l : progressListeners) {
            PlayerProgressListener listener = l.get();
            if (listener != null) {
                listener.onBufferingUpdate(milliseconds);
            }
        }
    }
}
