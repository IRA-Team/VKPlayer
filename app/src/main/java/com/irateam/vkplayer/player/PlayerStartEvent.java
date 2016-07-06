package com.irateam.vkplayer.player;

import com.irateam.vkplayer.models.Audio;

public class PlayerStartEvent extends PlayerEvent{

    protected PlayerStartEvent(int index, Audio audio) {
        super(index, audio);
    }
}
