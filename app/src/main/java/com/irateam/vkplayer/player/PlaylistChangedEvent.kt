package com.irateam.vkplayer.player

import com.irateam.vkplayer.event.Event
import com.irateam.vkplayer.model.Audio

class PlaylistChangedEvent : Event {

    val playlistBefore: List<Audio>
    val playlistAfter: List<Audio>

    constructor(playlistBefore: List<Audio>, playlistAfter: List<Audio>) {
        this.playlistBefore = playlistBefore
        this.playlistAfter = playlistAfter
    }
}