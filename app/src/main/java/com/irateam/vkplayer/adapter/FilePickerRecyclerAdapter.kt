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

package com.irateam.vkplayer.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.irateam.vkplayer.R
import com.irateam.vkplayer.ui.viewholder.DirectoryViewHolder
import com.irateam.vkplayer.ui.viewholder.FileViewHolder
import com.irateam.vkplayer.util.filepicker.FileMatcher
import com.irateam.vkplayer.util.filepicker.OnFileClickedListener
import com.irateam.vkplayer.util.filepicker.OnFilePickedListener
import com.irateam.vkplayer.util.filepicker.PickedStateProvider
import java.io.File

class FilePickerRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

	val directory: File
	val listFiles: List<File>

	private val pickedStateProvider: PickedStateProvider
	private val fileClickedListeners: Collection<OnFileClickedListener>
	private val filePickedListeners: Collection<OnFilePickedListener>
	private val fileMatchers: Collection<FileMatcher>

	private constructor(
			directory: File,
			pickedStateProvider: PickedStateProvider,
			fileClickedListeners: Collection<OnFileClickedListener>,
			filePickedListeners: Collection<OnFilePickedListener>,
			fileMatchers: Collection<FileMatcher>) {

		this.directory = directory
		this.pickedStateProvider = pickedStateProvider
		this.fileClickedListeners = fileClickedListeners
		this.filePickedListeners = filePickedListeners
		this.fileMatchers = fileMatchers

		val files = directory.listFiles()
		this.listFiles = if (files != null && files.isNotEmpty()) {
			var listFiles = files.toList()
			if (fileMatchers.isNotEmpty()) {
				listFiles = files.filter { file ->
					fileMatchers
							.map { it.match(file) }
							.any { it }
				}
			}
			listFiles.sortedBy { it.name }
					.sortedBy { it.isFile }
		} else {
			emptyList<File>()
		}

	}

	override fun getItemViewType(position: Int): Int {
		return if (listFiles[position].isFile) {
			TYPE_FILE
		} else {
			TYPE_DIRECTORY
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		return if (viewType == TYPE_DIRECTORY) {
			DirectoryViewHolder(inflater.inflate(R.layout.item_directory, parent, false))
		} else {
			FileViewHolder(inflater.inflate(R.layout.item_file, parent, false))
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val file = listFiles[position]
		when (holder) {
			is DirectoryViewHolder -> {
				holder.name.text = file.name
				holder.itemView.setOnClickListener { notifyFileClicked(file) }
				holder.filesCount.text = (file.list()?.size ?: 0).toString()
			}

			is FileViewHolder      -> {
				holder.name.text = file.name
				holder.itemView.setOnClickListener { notifyFileClicked(file) }
			}
		}
	}

	override fun getItemCount(): Int {
		return listFiles.size
	}

	private fun notifyFileClicked(file: File) {
		fileClickedListeners.forEach { it.onFileClicked(file) }
	}

	private fun notifyFilePicked(file: File) {
		filePickedListeners.forEach { it.onFilePicked(file) }
	}

	companion object {
		val TAG: String = FilePickerRecyclerAdapter::class.java.name

		private val TYPE_DIRECTORY = 1
		private val TYPE_FILE = 2
	}

	class Builder {
		private var showDirectories = true

		private var directory: File? = null
		private var pickedStateProvider: PickedStateProvider? = null
		private var fileClickedListeners: List<OnFileClickedListener> = emptyList()
		private var filePickedListeners: List<OnFilePickedListener> = emptyList()
		private var fileMatchers: List<FileMatcher> = emptyList()

		fun showDirectories(showDirectories: Boolean): Builder {
			this.showDirectories = showDirectories
			return this
		}

		fun setDirectory(directory: File): Builder {
			this.directory = directory
			return this
		}

		fun setPickedStateProvider(pickedStateProvider: PickedStateProvider): Builder {
			this.pickedStateProvider = pickedStateProvider
			return this
		}

		fun addFileClickedListener(fileClickedListener: OnFileClickedListener): Builder {
			this.fileClickedListeners += fileClickedListener
			return this
		}

		fun addFilePickedListener(filePickedListener: OnFilePickedListener): Builder {
			this.filePickedListeners += filePickedListener
			return this
		}

		fun addFileMatcher(fileMatcher: FileMatcher): Builder {
			this.fileMatchers += fileMatcher
			return this
		}

		fun build(): FilePickerRecyclerAdapter {
			if (showDirectories) {
				fileMatchers += FileMatcher.DirectoryMatcher()
			}

			return FilePickerRecyclerAdapter(
					directory = directory ?:
							throw IllegalStateException("You must set directory before build!"),
					pickedStateProvider = pickedStateProvider ?:
							throw IllegalStateException("You must set ${PickedStateProvider::class.java.name} before build!"),
					fileClickedListeners = fileClickedListeners,
					filePickedListeners = filePickedListeners,
					fileMatchers = fileMatchers)
		}
	}

}