package com.irateam.vkplayer.api.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.irateam.vkplayer.api.AbstractQuery
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.models.Metadata
import com.irateam.vkplayer.models.VKAudio
import com.irateam.vkplayer.notification.PlayerNotificationFactory
import com.mpatric.mp3agic.ID3v2
import com.mpatric.mp3agic.InvalidDataException
import com.mpatric.mp3agic.Mp3File
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MetadataService {

    val context: Context

    constructor(context: Context) {
        this.context = context
    }

    fun get(audio: VKAudio): Query<Metadata> {
        return GetMetadataQuery(audio)
    }

    private inner class GetMetadataQuery : AbstractQuery<Metadata> {

        val audio: VKAudio

        constructor(audio: VKAudio) : super() {
            this.audio = audio
        }

        override fun query(): Metadata {
            val bitrate: Int
            val size: Long
            val tags: ID3v2?
            var cover: Bitmap? = null
            var coverNotification: Bitmap? = null

            var mp3file: Mp3File? = null
            if (audio.isCached) {
                mp3file = Mp3File(audio.cachePath)
                size = mp3file.length
            } else {
                val buffer = ByteArray(BUFFER_SIZE)
                val temp = File.createTempFile(AUDIO_INFO_PREFIX,
                        audio.id.toString(),
                        context.cacheDir)

                val connection = URL(audio.url).openConnection()
                size = connection.contentLength.toLong()
                val input = connection.inputStream
                val output = FileOutputStream(temp)
                var bytes = input.read(buffer, 0, BUFFER_SIZE)
                while (!Thread.interrupted() && bytes != -1) {
                    output.write(buffer, 0, bytes)
                    try {
                        mp3file = Mp3File(temp.absolutePath)
                        break
                    } catch (ignore: InvalidDataException) {
                    }
                    bytes = input.read(buffer, 0, BUFFER_SIZE)
                }

                input.close()
                output.close()
            }

            if (mp3file == null) {
                throw IllegalStateException("No metadata provided!")
            } else {
                bitrate = mp3file.bitrate
                tags = mp3file.id3v2Tag

                if (tags != null) {
                    val image = tags.albumImage
                    if (image != null) {
                        cover = BitmapFactory.decodeByteArray(image, 0, image.size)
                        coverNotification = PlayerNotificationFactory.scaleNotification(context, cover!!)
                    }
                }

                return Metadata(
                        bitrate,
                        size,
                        tags,
                        cover,
                        coverNotification)
            }
        }
    }

    companion object {

        val BUFFER_SIZE = 2048
        val AUDIO_INFO_PREFIX = "AUDIO_INFO_"
    }
}
