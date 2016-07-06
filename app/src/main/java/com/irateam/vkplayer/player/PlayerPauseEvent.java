package com.irateam.vkplayer.player;

import com.irateam.vkplayer.models.Audio;

public class PlayerPauseEvent extends PlayerEvent{

    protected PlayerPauseEvent(int index, Audio audio) {
        super(index, audio);
    }
}
