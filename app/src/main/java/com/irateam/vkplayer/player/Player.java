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

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import static com.irateam.vkplayer.player.Player.RepeatState.ALL_REPEAT;
import static com.irateam.vkplayer.player.Player.RepeatState.NO_REPEAT;
import static com.irateam.vkplayer.player.Player.RepeatState.ONE_REPEAT;

public class Player extends MediaPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {

    private static final Player instance = new Player();

    private final EventBus eventBus = EventBus.getDefault();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private List<Audio> queue = new ArrayList<>();
    private final Stack<Audio> randomStack = new Stack<>();
    private final Random random = new Random();

    private Audio playingAudio;
    private ProgressThread currentProgressThread;
    private int pauseTime;

    //Player state
    private boolean isReady = false;
    private boolean randomState = false;
    private RepeatState repeatState = NO_REPEAT;

    private Player() {
        super();
        setAudioStreamType(AudioManager.STREAM_MUSIC);
        setOnPreparedListener(this);
        setOnCompletionListener(this);
    }

    public static Player getInstance() {
        return instance;
    }

    public Audio getPlayingAudio() {
        return playingAudio;
    }

    public Integer getPlayingAudioIndex() {
        return queue.indexOf(playingAudio);
    }

    public List<Audio> getQueue() {
        return queue;
    }

    public void setQueue(List<Audio> queue) {
        this.queue = queue;
    }

    public void play(int index) {
        playingAudio = queue.get(index);
        try {
            reset();
            stopProgress();
            setOnBufferingUpdateListener(null);
            setDataSource(playingAudio.getPlayingUrl());
            prepareAsync();
            eventBus.post(new PlayerPlayEvent(index, playingAudio));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        if (isReady() && playingAudio != null) {
            seekTo(pauseTime);
            start();
            eventBus.post(new PlayerResumeEvent(getPlayingAudioIndex(), playingAudio));
        }
    }

    public void stop() {
        super.reset();
        eventBus.post(new PlayerStopEvent(getPlayingAudioIndex(), playingAudio));
        playingAudio = null;
    }

    public void pause() {
        if (isReady()) {
            if (isPlaying()) {
                super.pause();
                pauseTime = getCurrentPosition();
            }
        }
        eventBus.post(new PlayerPauseEvent(getPlayingAudioIndex(), playingAudio));
    }

    public int getPauseTime() {
        return pauseTime;
    }

    public void next() {
        int nextIndex;
        if (queue == null || queue.size() == 0) {
            stop();
            return;
        }
        if (randomState) {
            int size = queue.size();
            do
                nextIndex = random.nextInt(size);
            while (size > 1 &&
                    getPlayingAudioIndex() == nextIndex);
            randomStack.push(playingAudio);
        } else {
            nextIndex = getPlayingAudioIndex() + 1;
            if (queue.size() == nextIndex) {
                nextIndex = 0;
            }
        }
        reset();
        play(nextIndex);
    }

    public void previous() {
        int previousIndex;
        if (queue == null || queue.size() == 0) {
            stop();
            return;
        }
        if (randomState && !randomStack.empty()) {
            previousIndex = queue.indexOf(randomStack.pop());
        } else {
            previousIndex = getPlayingAudioIndex();
            if (previousIndex == 0) {
                previousIndex = queue.size() - 1;
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
            randomStack.clear();
        }
        return randomState;
    }

    public void setRandomState(boolean randomState) {
        this.randomState = randomState;
        if (randomState) {
            randomStack.clear();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (repeatState == NO_REPEAT && playingAudio == queue.get(queue.size() - 1)) {
            eventBus.post(new PlayerStopEvent(getPlayingAudioIndex(), playingAudio));
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
        isReady = true;
        start();
        startProgress();
        setOnBufferingUpdateListener(this);

        eventBus.post(new PlayerPlayEvent(getPlayingAudioIndex(), playingAudio));
    }

    @Override
    public void reset() {
        super.reset();
        isReady = false;
    }

    public boolean isReady() {
        return isReady;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        notifyBufferingUpdate(percent * getDuration() / 100);
    }

    private void notifyPlayerProgressChanged() {
        uiHandler.post(() -> {
            final int currentPosition = getCurrentPosition();
            eventBus.post(new PlayerProgressChangedEvent(currentPosition));
        });
    }

    private void notifyBufferingUpdate(int milliseconds) {
        uiHandler.post(() -> {
            eventBus.post(new PlayerBufferingUpdateEvent(milliseconds));
        });
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

            try {
                while (!Thread.interrupted()) {
                    if (isPlaying()) {
                        notifyPlayerProgressChanged();
                    }
                    Thread.sleep(500);
                }
            } catch (InterruptedException ignore) {
            }
        }
    }

    public enum RepeatState {
        NO_REPEAT,
        ONE_REPEAT,
        ALL_REPEAT
    }
}
