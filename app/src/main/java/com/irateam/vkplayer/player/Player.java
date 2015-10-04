package com.irateam.vkplayer.player;

import android.media.MediaPlayer;

import com.vk.sdk.api.model.VKApiAudio;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import static com.irateam.vkplayer.player.Player.RepeatState.ALL_REPEAT;
import static com.irateam.vkplayer.player.Player.RepeatState.NO_REPEAT;
import static com.irateam.vkplayer.player.Player.RepeatState.ONE_REPEAT;

public class Player extends MediaPlayer implements MediaPlayer.OnCompletionListener {

    public static final String proxyURL = "http://localhost:8080/";

    private static Player instance;

    public synchronized static Player getInstance() {
        if (instance == null) {
            instance = new Player();
        }
        return instance;
    }

    private Player() {
        super();
        setOnCompletionListener(this);
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

    public List<VKApiAudio> getList() {
        return list;
    }

    public void setList(List<VKApiAudio> list) {
        this.list = list;
    }

    public void play(int index) {
        /*try {
            setDataSource(proxyURL + index);
            prepare();
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        playingAudio = list.get(index);
        notifyAudioChanged(index, list.get(index));
    }

    public void play() {
        if (playingAudio != null) {

        }
    }

    public void stop() {
        if (isPlaying() && playingAudio != null) {
            playingAudio = null;
            super.stop();
        }
    }

    public void pause() {
        if (isPlaying()) {
            super.pause();
        }
    }

    public void next() {
        int nextIndex;
        if (randomState) {
            nextIndex = random.nextInt(list.size());
            randomStack.push(playingAudio);
        } else {
            nextIndex = list.indexOf(playingAudio) + 1;
            if (list.size() == nextIndex) {
                nextIndex = 0;
            }
        }
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
        if (randomState) {
            randomStack.push(playingAudio);
        }
    }

    //Listeners
    private List<WeakReference<Listener>> listeners = new ArrayList<>();

    public interface Listener {
        void onAudioChanged(int position, VKApiAudio audio);
    }

    public void addListener(Listener listener) {
        listeners.add(new WeakReference<>(listener));
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void notifyAudioChanged(int position, VKApiAudio audio) {
        for (WeakReference<Listener> l : listeners) {
            Listener listener = l.get();
            if (listener != null) {
                listener.onAudioChanged(position, audio);
            }
        }
    }

    public enum RepeatState {
        NO_REPEAT,
        ONE_REPEAT,
        ALL_REPEAT
    }

}
