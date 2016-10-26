/*
 * Copyright (C)r 2016 IRA-Team
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
import android.util.Log
import com.irateam.vkplayer.api.AbstractQuery
import com.irateam.vkplayer.api.ProgressableAbstractQuery
import com.irateam.vkplayer.api.ProgressableQuery
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.database.AudioLocalIndexedDatabase
import com.irateam.vkplayer.event.AudioScannedEvent
import com.irateam.vkplayer.model.LocalAudio
import com.irateam.vkplayer.util.extension.e
import com.irateam.vkplayer.util.extension.process
import com.irateam.vkplayer.util.extension.splitToPartitions
import com.mpatric.mp3agic.Mp3File
import java.io.File
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class LocalAudioService {

	private val context: Context
	private val database: AudioLocalIndexedDatabase
	private val audioConverterService: AudioConverterService


	constructor(context: Context) {
		this.context = context
		this.database = AudioLocalIndexedDatabase(context)
		this.audioConverterService = AudioConverterService(context)
	}

	fun scan(): ProgressableQuery<List<LocalAudio>, AudioScannedEvent> {
		val root = File("/storage")
		return ScanAndIndexAudioQuery(root)
	}

	fun getAllIndexed(): Query<List<LocalAudio>> {
		return IndexedAudioQuery()
	}

	fun removeFromFilesystem(audios: Collection<LocalAudio>): Query<Collection<LocalAudio>> {
		return RemoveFromFilesystemQuery(audios)
	}

	private inner class IndexedAudioQuery : AbstractQuery<List<LocalAudio>>() {

		override fun query() = database.getAll()
	}

	private inner class ScanAndIndexAudioQuery : ProgressableAbstractQuery<List<LocalAudio>, AudioScannedEvent> {

		val root: File

		constructor(root: File) : super() {
			this.root = root
		}

		override fun query(): List<LocalAudio> {
			val audios = root.walk()
					.toList()
					.map { e(it); it }
					.filter { !it.isDirectory }
					.filter { it.name.endsWith(".mp3") }
					.map {
						try {
							Mp3File(it.path)
						} catch (e: Exception) {
							e.printStackTrace()
							e(it.path)
							null
						}
					}
					.filterNotNull()


			val i = AtomicInteger()
			val tasks = audios.splitToPartitions(CORE_COUNT).map {
				Callable {
					it.map { audioConverterService.createLocalAudioFromMp3(it) }
							.process {
								try {
									database.index(it)
									Log.i(TAG, "Stored $it")
								} catch (ignore: Exception) {
								}

								notifyProgress(AudioScannedEvent(
										it,
										i.incrementAndGet(),
										audios.count()))
							}
							.toList()
				}
			}

			return CONVERTER_EXECUTOR.invokeAll(tasks)
					.map { it.get() }
					.flatMap { it }
		}
	}

	private inner class RemoveFromFilesystemQuery : AbstractQuery<Collection<LocalAudio>> {

		val audios: Collection<LocalAudio>

		/**
		 * Always make copy
		 */
		constructor(audios: Collection<LocalAudio>) : super() {
			this.audios = audios.toList()
		}

		override fun query(): Collection<LocalAudio> {
			val removed = ArrayList<LocalAudio>()
			audios.forEach {
				val file = File(it.path)
				if (!file.exists() || file.delete()) {
					database.delete(it)
					removed.add(it)
				} else {
					throw AccessDeniedException(
							file = file,
							reason = "The error occurred during deleting file.")
				}
			}
			return removed
		}
	}

	companion object {
		val TAG: String = LocalAudioService::class.java.name

		val CORE_COUNT = Runtime.getRuntime().availableProcessors() * 2
		val CONVERTER_EXECUTOR: ExecutorService = Executors.newFixedThreadPool(CORE_COUNT)
	}
}