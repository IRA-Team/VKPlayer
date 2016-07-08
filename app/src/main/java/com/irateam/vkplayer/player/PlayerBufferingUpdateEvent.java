package com.irateam.vkplayer.player;

public class PlayerBufferingUpdateEvent {

    private final int milliseconds;

    public PlayerBufferingUpdateEvent(int milliseconds) {
        this.milliseconds = milliseconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }
}
