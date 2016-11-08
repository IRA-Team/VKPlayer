package com.irateam.vkplayer.api.service

import android.content.Context
import com.irateam.vkplayer.R
import com.irateam.vkplayer.model.LocalAudio
import com.irateam.vkplayer.util.extension.e
import com.mpatric.mp3agic.Mp3File
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

class AudioConverterService {

    private val nameDiscover: LocalAudioNameDiscover

    private val unknownArtist: String
    private val unknownTitle: String

    constructor(context: Context) {

        this.unknownArtist = context.getString(R.string.unknown_artist)
        this.unknownTitle = context.getString(R.string.unknown_title)

        this.nameDiscover = LocalAudioNameDiscover()
    }

    fun toLocalAudioFromFile(file: File): LocalAudio? = try {
        val audioFile = AudioFileIO.read(file)
        val duration = audioFile.audioHeader.trackLength
        val tag = audioFile.tag

        if (tag != null ) {
            LocalAudio(
                    tag.getFirst(FieldKey.ARTIST),
                    tag.getFirst(FieldKey.TITLE),
                    duration,
                    file.path)
        } else {
            val name = file.nameWithoutExtension
            val titleArtist = nameDiscover.getTitleAndArtist(name)

            val artist = titleArtist.artist ?: unknownArtist
            val title = titleArtist.title ?: unknownTitle

            LocalAudio(artist,
                    title,
                    duration,
                    file.path)
        }
    } catch (e: Exception) {
        e(TAG, "An error occurred during converting to LocalAudio")
        e.printStackTrace()
        null
    }

    fun createLocalAudioFromMp3(mp3: Mp3File): LocalAudio = if (mp3.hasId3v2Tag()) {
        val artist = mp3.id3v2Tag.artist ?: unknownArtist
        val title = mp3.id3v2Tag.title ?: unknownTitle
        mp3.id3v2Tag.albumImage

        LocalAudio(artist,
                title,
                mp3.lengthInSeconds.toInt(),
                mp3.filename)
    } else {
        val name = File(mp3.filename).nameWithoutExtension
        val titleArtist = nameDiscover.getTitleAndArtist(name)

        val artist = titleArtist.artist ?: unknownArtist
        val title = titleArtist.title ?: unknownTitle

        LocalAudio(artist,
                title,
                mp3.lengthInSeconds.toInt(),
                mp3.filename)
    }

    companion object {
        val TAG: String = AudioConverterService::class.java.name
    }
}