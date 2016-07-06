package com.irateam.vkplayer.player;

import com.irateam.vkplayer.models.Audio;

public class PlayerResumeEvent extends PlayerEvent {

    protected PlayerResumeEvent(int index, Audio audio) {
        super(index, audio);
    }
}
