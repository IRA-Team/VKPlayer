package com.irateam.vkplayer.player

import com.irateam.vkplayer.model.Audio

class PlayerErrorEvent : PlayerEvent {

    val cause: Throwable

    constructor(cause: Throwable, index: Int, audio: Audio) : super(index, audio) {
        this.cause = cause
    }
}