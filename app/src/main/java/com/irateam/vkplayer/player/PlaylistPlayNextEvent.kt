package com.irateam.vkplayer.player

import com.irateam.vkplayer.event.Event

class PlaylistPlayNextEvent : Event {

    val playNextPosition: Int
    val playNextSize: Int
    val playlistPosition: Int

    constructor(playNextPosition: Int, playNextSize: Int,  playlistPosition: Int) {
        this.playNextPosition = playNextPosition
        this.playNextSize = playNextSize
        this.playlistPosition = playlistPosition
    }
}