package com.irateam.vkplayer.player;

import com.irateam.vkplayer.models.Audio;

public abstract class PlayerEvent {

    private final int index;
    private final Audio audio;

    protected PlayerEvent(int index, Audio audio) {
        this.index = index;
        this.audio = audio;
    }

    public int getIndex() {
        return index;
    }

    public Audio getAudio() {
        return audio;
    }
}
