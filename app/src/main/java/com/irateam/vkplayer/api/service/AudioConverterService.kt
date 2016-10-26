package com.irateam.vkplayer.api.service

import android.content.Context
import com.irateam.vkplayer.R
import com.irateam.vkplayer.model.LocalAudio
import com.irateam.vkplayer.util.extension.d
import com.mpatric.mp3agic.Mp3File
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
}