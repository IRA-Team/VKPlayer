package com.irateam.vkplayer.player;

import android.media.MediaPlayer;

import com.vk.sdk.api.model.VKApiAudio;

import java.util.LinkedHashMap;

public class Player {
    private static Player instance;

    public synchronized static Player getInstance() {
        if (instance == null) {
            instance = new Player();
        }
        return instance;
    }

    private Player() {
    }

    private MediaPlayer player;
    private LinkedHashMap<Integer, VKApiAudio> list;


    public VKApiAudio getAudio(int index) {
        return list.get(index);
    }

    public void play() {

    }

    public void stop() {

    }
}
