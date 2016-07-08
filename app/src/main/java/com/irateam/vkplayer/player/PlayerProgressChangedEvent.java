package com.irateam.vkplayer.player;

public class PlayerProgressChangedEvent {

    private final int milliseconds;

    public PlayerProgressChangedEvent(int milliseconds) {
        this.milliseconds = milliseconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }
}
