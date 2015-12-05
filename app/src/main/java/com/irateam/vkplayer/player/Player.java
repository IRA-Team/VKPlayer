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

package com.irateam.vkplayer.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import com.irateam.vkplayer.models.Audio;

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

    private ProgressThread currentProgressThread;

    private boolean stateReady = false;
    private boolean randomState = false;
    private RepeatState repeatState = NO_REPEAT;

    private int pauseTime;

    public Player() {
        super();
        setAudioStreamType(AudioManager.STREAM_MUSIC);
        setOnPreparedListener(this);
        setOnCompletionListener(this);
    }

    private List<Audio> list = new ArrayList<>();
    private Stack<Audio> randomStack = new Stack<>();
    private Random random = new Random();

    private Audio playingAudio;

    public Audio getPlayingAudio() {
        return playingAudio;
    }

    public Integer getPlayingAudioIndex() {
        return list.indexOf(playingAudio);
    }

    public List<Audio> getList() {
        return list;
    }

    public void setList(List<Audio> list) {
        this.list = list;
    }

    public void play(int index) {
        playingAudio = list.get(index);
        try {
            reset();
            stopProgress();
            setOnBufferingUpdateListener(null);
            setDataSource(playingAudio.getPlayingUrl());
            prepareAsync();
            notifyPlayerEvent(index, playingAudio, PlayerEvent.START);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        if (isReady() && playingAudio != null) {
            seekTo(pauseTime);
            start();
            notifyPlayerEvent(getPlayingAudioIndex(), playingAudio, PlayerEvent.RESUME);
        }
    }

    public void stop() {
        super.reset();
        notifyPlayerEvent(getPlayingAudioIndex(), playingAudio, PlayerEvent.STOP);
        playingAudio = null;
    }

    public void pause() {
        if (isReady()) {
            if (isPlaying()) {
                super.pause();
                pauseTime = getCurrentPosition();
            }
        }
        notifyPlayerEvent(getPlayingAudioIndex(), playingAudio, PlayerEvent.PAUSE);
    }

    public int getPauseTime() {
        return pauseTime;
    }


    public void next() {
        int nextIndex;
        if (list == null || list.size() == 0) {
            stop();
            return;
        }
        if (randomState) {
            int size = list.size();
            do
                nextIndex = random.nextInt(size);
            while (size > 1 &&
                    getPlayingAudioIndex() == nextIndex);
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
        if (list == null || list.size() == 0) {
            stop();
            return;
        }
        if (randomState && !randomStack.empty()) {
            previousIndex = list.indexOf(randomStack.pop());
        } else {
            previousIndex = getPlayingAudioIndex();
            if (previousIndex == 0) {
                previousIndex = list.size() - 1;
            } else if (previousIndex == -1) {
                previousIndex = 0;
            } else {
                previousIndex -= 1;
            }
        }
        reset();
        play(previousIndex);
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        if (isReady()) {
            super.seekTo(msec);
            pauseTime = msec;
        }
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
        stateReady = true;
        start();
        startProgress();
        setOnBufferingUpdateListener(this);
        notifyPlayerEvent(getPlayingAudioIndex(), playingAudio, PlayerEvent.PLAY);
    }

    @Override
    public void reset() {
        super.reset();
        stateReady = false;
    }

    public boolean isReady() {
        return stateReady;
    }

    //Listeners
    private List<WeakReference<PlayerEventListener>> listeners = new ArrayList<>();

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        notifyBufferingUpdate(percent * getDuration() / 100);
    }

    public interface PlayerEventListener {
        void onEvent(int position, Audio audio, PlayerEvent event);
    }

    public void addPlayerEventListener(PlayerEventListener listener) {
        listeners.add(new WeakReference<>(listener));
    }

    public void removePlayerEventListener(PlayerEventListener listener) {
        listeners.remove(listener);
    }

    private void notifyPlayerEvent(int position, Audio audio, PlayerEvent event) {
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
        START,
        PLAY,
        PAUSE,
        RESUME,
        STOP
    }

    //Progress Listener
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
        new Handler(Looper.getMainLooper()).post(() -> {
            for (WeakReference<PlayerProgressListener> l : progressListeners) {
                PlayerProgressListener listener = l.get();
                if (listener != null) {
                    listener.onProgressChanged(getCurrentPosition());
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

    public void startProgress() {
        currentProgressThread = new ProgressThread();
        currentProgressThread.start();
    }

    public void stopProgress() {
        if (currentProgressThread != null && !currentProgressThread.isInterrupted()) {
            currentProgressThread.interrupt();
        }
    }

    private class ProgressThread extends Thread {

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    if (isPlaying()) {
                        notifyPlayerProgressChanged();
                    }
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //Nothing
                }
            }
        }

    }
}
