/*
 * Copyright (C) 2016 IRA-Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irateam.vkplayer.api.service

import android.content.Context
import com.irateam.vkplayer.api.AbstractQuery
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.model.LocalAudio
import com.irateam.vkplayer.util.extension.splitToPartitions
import com.irateam.vkplayer.util.extension.w
import com.mpatric.mp3agic.Mp3File
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VKExternalAudioService {

	private val audioConverterService: AudioConverterService

	constructor(context: Context) {
		this.audioConverterService = AudioConverterService(context)
	}

	fun getExternal(): Query<List<LocalAudio>> {
		return VKExternalAudioQuery()
	}

	fun removeFromFilesystem(audios: Collection<LocalAudio>): Query<List<LocalAudio>> {
		TODO()
	}

	private fun File.toMp3File(): Mp3File? = try {
		Mp3File(path)
	} catch (e: Exception) {
		w("File can't be converted to Mp3File $path")
		null
	}

	private inner class VKExternalAudioQuery : AbstractQuery<List<LocalAudio>>() {

		override fun query(): List<LocalAudio> {
			return possibleDirectories
					.map(::File)
					.map { it.walk() }
					.flatMap { it.toList() }
					.filter { !it.isDirectory }
					.filter { it.extension.isEmpty() }
					.splitToPartitions(CORE_COUNT)
					.map { Mp3ConvertCallable(it) }
					.map { CONVERTER_EXECUTOR.submit(it) }
					.map { it.get() }
					.flatten()
		}

		private inner class Mp3ConvertCallable : Callable<List<LocalAudio>> {

			val files: List<File>

			constructor(files: List<File>) {
				this.files = files
			}

			override fun call(): List<LocalAudio> {
				return files
						.map { it.toMp3File() }
						.filterNotNull()
						.map { audioConverterService.createLocalAudioFromMp3(it) }
			}


		}
	}

	companion object {
		private val possibleDirectories = listOf(
				"/sdcard/.vkontakte/cache/audio")

		val CORE_COUNT = Runtime.getRuntime().availableProcessors() * 2
		val CONVERTER_EXECUTOR: ExecutorService = Executors.newFixedThreadPool(CORE_COUNT)
	}
}