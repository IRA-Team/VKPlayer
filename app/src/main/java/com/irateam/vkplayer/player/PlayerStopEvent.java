package com.irateam.vkplayer.player;

import com.irateam.vkplayer.models.Audio;

public class PlayerStopEvent extends PlayerEvent{

    protected PlayerStopEvent(int index, Audio audio) {
        super(index, audio);
    }
}
