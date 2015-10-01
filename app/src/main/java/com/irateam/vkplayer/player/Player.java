package com.irateam.vkplayer.player;

import android.media.MediaPlayer;

import com.vk.sdk.api.model.VKApiAudio;

import java.io.IOException;
import java.util.List;

public class Player extends MediaPlayer {

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
    }

    private List<VKApiAudio> list;

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
        try {
            setDataSource(proxyURL + index);
            prepare();
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        int nextIndex = list.indexOf(playingAudio) + 1;
        if (list.size() == nextIndex) {
            nextIndex = 0;
        }
        play(nextIndex);
    }

    public void previous() {
        int previousIndex = list.indexOf(playingAudio) - 1;
        if (previousIndex == -1) {
            previousIndex = list.size() - 1;
        }
        play(previousIndex);
    }
}
