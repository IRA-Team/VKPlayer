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
    override var originalPlaylist: List<Audio> = emptyList()
        private set

    override var audio: Audio? = null
        private set

    override var random by observable(false) { property, oldRandom, newRandom ->
        val playlistBefore = playlist
        setupQueues(
                head = audio,
                shouldIncludeHead = false,
                random = newRandom)
        EventBus.post(PlaylistChangedEvent(playlistBefore, playlist))
    }

    /**
     * Inner playlist
     */
    override var playlist: ArrayList<Audio> = ArrayList()

    /**
     * Holds audios in order in which they would be played
     */
    private var queue: ArrayList<Audio> = ArrayList()

    /**
     * Holds all previously played audios
     */
    private val history: Stack<Audio> = Stack()

    override val queueSize: Int
        get() = queue.size

    /**
     * Position of playing audio relatively to original playlist
     */
    override val audioPosition: Int
        get() = playlist.indexOf(Player.audio)

    override fun setQueue(audios: Collection<Audio>, head: Audio?, random: Boolean) {
        originalPlaylist = ArrayList(audios)
        history.clear()
        audio = null
        setupQueues(
                head = head,
                shouldIncludeHead = true,
                random = random)
    }

    override fun addToQueue(audios: Collection<Audio>) {
        TODO()
    }

    override fun addToPlayNext(audios: Collection<Audio>) {
        val toAdd = audios.map(Audio::clone)
        playlist.addAll(audioPosition + 1, toAdd)
        queue.addAll(0, toAdd)
    }

    private fun setupQueues(head: Audio?, shouldIncludeHead: Boolean, random: Boolean) {
        d(TAG, "Set up queues")
        if (!random) {
            history.clear()
            setupDefaultQueue(
                    head = head,
                    shouldIncludeHead = shouldIncludeHead)
        } else {
            setupShuffledQueue(
                    head = head,
                    shouldIncludeHead = shouldIncludeHead)
        }
    }

    private fun setupDefaultQueue(head: Audio?, shouldIncludeHead: Boolean) {
        d(TAG, "Set up default queue with params: head - $head, shouldIncludeHead - $shouldIncludeHead")
        playlist = ArrayList(originalPlaylist)

        if (head != null) {
            val headIndex = playlist.indexOf(head)
            val firstPart = if (headIndex > 0) {
                playlist.subList(0, headIndex)
            } else {
                emptyList<Audio>()
            }
            val lastPart = if (headIndex != playlist.size - 1) {
                playlist.subList(headIndex + 1, playlist.size)
            } else {
                emptyList<Audio>()
            }
            queue = ArrayList<Audio>().apply {
                if (shouldIncludeHead) {
                    add(head)
                }

                addAll(lastPart)
                addAll(firstPart)
            }
        } else {
            queue = ArrayList(playlist)
        }
    }

    private fun setupShuffledQueue(head: Audio?, shouldIncludeHead: Boolean) {
        d(TAG, "Set up shuffled queue with params: head - $head, shouldIncludeHead - $shouldIncludeHead")
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
        if (head != null && !shouldIncludeHead && shuffled.isNotEmpty()) {
            shuffled.removeAt(0)
        }
        queue = shuffled
    }

    override fun pollNextAudio(): Audio {
        d(TAG, "Polling next audio")
        val currentAudio = audio
        if (currentAudio != null) {
            history.push(currentAudio)
            d(TAG, "Current audio is not null. Audio added to history.")
        } else {
            e(TAG, "Current audio is null. Audio didn't added to history")
        }

        if (queue.isEmpty()) {
            i(TAG, "Queue is empty. Set up new queue")
            val beforePlaylist = playlist
            setupQueues(
                    head = audio,
                    shouldIncludeHead = random,
                    random = random)
            EventBus.post(PlaylistChangedEvent(beforePlaylist, playlist))
            d(TAG, "New queue: $queue")
        }
        val audio = queue[0]
        queue.removeAt(0)

        i(TAG, "Polled audio: $audio")
        this.audio = audio
        return audio
    }

    override fun pollPreviousAudio(): Audio {
        d(TAG, "Polling previous audio")
        val currentAudio = audio
        if (currentAudio != null) {
            queue.add(0, currentAudio)
            d(TAG, "Current audio is not null. Audio added to queue.")
        } else {
            e(TAG, "Current audio is null. Audio didn't added to queue")
        }

        val audio = when {
            history.isNotEmpty() -> {
                d(TAG, "History stack is not empty. Polling audio from it")
                history.pop()
            }

            else -> {
                d(TAG, "History stack is empty. Polling audio from playlist")
                val index = playlist.indexOf(Player.audio)
                d(TAG, "Index of current audio in playlist: $index")
                if (index == 0 || index == -1) {
                    playlist.last()
                } else {
                    playlist[index - 1]
                }
            }
        }
        i(TAG, "Polled audio: $audio")
        this.audio = audio
        return audio
    }

    override fun reset() {
        d(TAG, "Reset")
        audio = null
        history.clear()
        queue = ArrayList()
    }

    private fun logPlaylist() {
        i(TAG, "Playlist: $playlist")
        i(TAG, "")
        i(TAG, "HistoryStack: Size = ${history.size}")
        i(TAG, "HistoryStack: $history")
        i(TAG, "")
        i(TAG, "Queue: Size = ${queue.size}")
        i(TAG, "Queue: $queue")
        i(TAG, "=================================================")
    }

    companion object {

        val TAG: String = PlaylistManager::class.java.name
    }
}