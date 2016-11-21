package com.irateam.vkplayer.player

import com.irateam.vkplayer.model.Audio
import com.irateam.vkplayer.util.EventBus
import com.irateam.vkplayer.util.extension.d
import com.irateam.vkplayer.util.extension.e
import com.irateam.vkplayer.util.extension.i
import java.util.*
import kotlin.properties.Delegates.observable

class PlaylistManager : Player.PlaylistManager {

    /**
     * Original playlist.
     * Inner playlist is built based on original playlist.
     * Also uses for providing correct audio position.
     * Must be changed only by calling setQueue()
     */
    override var originalPlaylist: ArrayList<Audio> = ArrayList()
        private set

    override var audio: Audio? = null
        private set

    override var random by observable(false) { property, oldRandom, newRandom ->
        val playlistBefore = playlist
        setupQueues(head = audio, shouldIncludeHead = false)
        EventBus.post(PlaylistChangedEvent(playlistBefore, playlist))
    }

    /**
     * Inner playlist
     */
    override var playlist: ArrayList<Audio> = ArrayList()

    /**
     * Holds all previously played audios
     */
    private val playlistHistory: Deque<ArrayList<Audio>> = ArrayDeque()

    override val playlistSize: Int
        get() = playlist.size

    /**
     * Position of playing audio relatively to original playlist
     */
    override val audioIndex: Int
        get() = playlist.indexOf(audio)

    private val isFirst: Boolean
        get() = playlist.isNotEmpty() && playlist.first() == audio

    private val isLast: Boolean
        get() = playlist.isNotEmpty() && playlist.last() == audio

    override fun setQueue(audios: Collection<Audio>, head: Audio?) {
        originalPlaylist = ArrayList(audios)
        reset()
        setupQueues(head = head, shouldIncludeHead = true)
    }

    override fun addToQueue(audios: Collection<Audio>) {
        TODO()
    }

    override fun addToPlayNext(audios: Collection<Audio>) {
        val toAdd = audios.map(Audio::clone)
        playlist.addAll(audioIndex + 1, toAdd)

        val originalPosition = originalPlaylist.indexOf(audio)
        originalPlaylist.addAll(originalPosition + 1, toAdd)
    }

    private fun setupQueues(head: Audio?, shouldIncludeHead: Boolean) {
        d(TAG, "Set up playlist")
        if (!random) {
            playlistHistory.clear()

            setupDefaultQueue()
        } else {
            setupShuffledQueue(head = head, shouldIncludeHead = shouldIncludeHead)
        }
    }

    private fun setupDefaultQueue() {
        d(TAG, "Set up default playlist")
        playlist = ArrayList(originalPlaylist)
    }

    private fun setupShuffledQueue(head: Audio?, shouldIncludeHead: Boolean) {
        d(TAG, "Set up shuffled playlist with params: head - $head, shouldIncludeHead - $shouldIncludeHead")
        val shuffled = ArrayList(originalPlaylist)
        Collections.shuffle(shuffled)
        if (head != null) {
            val index = shuffled.indexOf(head)
            if (index != -1) {
                shuffled.removeAt(index)
            }
            shuffled.add(0, head)
        }
        playlist = ArrayList(shuffled)
    }

    override fun pollNextAudio(): Audio {
        d(TAG, "Polling next audio")
        val audio = if (isLast) {
            if (random) {
                i(TAG, "Playlist ended. Set up new queue")
                val beforePlaylist = playlist
                playlistHistory.push(beforePlaylist)
                setupShuffledQueue(audio, true)
                EventBus.post(PlaylistChangedEvent(beforePlaylist, playlist))
            }
            playlist.first()
        } else {
            playlist[audioIndex + 1]
        }

        i(TAG, "Polled audio: $audio")
        this.audio = audio
        return audio
    }

    override fun poll(audio: Audio): Audio {
        this.audio = audio
        return audio
    }

    override fun pollPreviousAudio(): Audio {
        d(TAG, "Polling previous audio")
        val audio = if (isFirst && playlistHistory.isNotEmpty()) {
            val playlistBefore = playlist
            playlist = playlistHistory.poll()
            EventBus.post(PlaylistChangedEvent(playlistBefore, playlist))
            playlist.last()
        } else {
            val index = playlist.indexOf(Player.audio)
            if (index == 0 || index == -1) {
                playlist.last()
            } else {
                playlist[index - 1]
            }
        }
        i(TAG, "Polled audio: $audio")
        this.audio = audio
        return audio
    }

    override fun reset() {
        d(TAG, "Reset")
        audio = null
        playlistHistory.clear()
    }

    companion object {

        val TAG: String = PlaylistManager::class.java.name
    }
}