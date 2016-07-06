package com.irateam.vkplayer.player;

import com.irateam.vkplayer.models.Audio;

public class PlayerPlayEvent extends PlayerEvent{

    protected PlayerPlayEvent(int index, Audio audio) {
        super(index, audio);
    }
}
