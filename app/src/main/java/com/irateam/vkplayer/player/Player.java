package com.irateam.vkplayer.player;

import android.media.MediaPlayer;

import com.irateam.vkplayer.services.AudioService;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VKApiAudio;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Player extends MediaPlayer implements AudioService.Listener {

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
        /*try {
            setDataSource(proxyURL + index);
            prepare();
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        notifyAudioChanged(list.get(index));
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

    @Override
    public void onComplete(List<VKApiAudio> list) {

    }

    @Override
    public void onError(VKError error) {

    }

    //Listeners
    private List<WeakReference<Listener>> listeners = new ArrayList<>();

    public interface Listener {
        void onAudioChanged(VKApiAudio audio);
    }

    public void addListener(Listener listener) {
        listeners.add(new WeakReference<Listener>(listener));
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void notifyAudioChanged(VKApiAudio audio) {
        for (WeakReference<Listener> l : listeners) {
            l.get().onAudioChanged(audio);
        }
    }
}
