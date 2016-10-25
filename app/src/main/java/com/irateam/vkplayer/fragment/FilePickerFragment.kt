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

package com.irateam.vkplayer.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.irateam.vkplayer.R
import com.irateam.vkplayer.adapter.FilePickerRecyclerAdapter
import com.irateam.vkplayer.util.extension.allFiles
import com.irateam.vkplayer.util.extension.getViewById
import com.irateam.vkplayer.util.filepicker.OnFileClickedListener
import com.irateam.vkplayer.util.filepicker.OnFilePickedStateChangeListener
import com.irateam.vkplayer.util.filepicker.PickedStateProvider
import java.io.File
import java.util.*

class FilePickerFragment : Fragment(),
		PickedStateProvider,
		OnFileClickedListener,
		OnFilePickedStateChangeListener,
		BackPressedListener {

	private val pickedFiles: HashSet<File> = HashSet()

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {

		return inflater.inflate(R.layout.fragment_file_picker_container, container, false)
	}

	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		addFragment(File("/"))
	}

	override fun onFileClicked(file: File) {
		if (file.isDirectory) {
			addFragment(file)
		}
	}

	override fun onChange(file: File, isPicked: Boolean) {
		file.allFiles().let {
			if (isPicked) pickedFiles.addAll(it) else pickedFiles.removeAll(it)
		}
	}

	override fun onBackPressed(): Boolean {
		childFragmentManager.popBackStack()
		return true
	}

	private fun addFragment(file: File) {
		val fragment = FilePickerEntry.newInstance(
				file = file,
				pickedStateProvider = this,
				fileClickedListener = this,
				filePickedStateChangeListener = this)

		childFragmentManager.beginTransaction()
				.setCustomAnimations(
						R.anim.slide_in_left,
						R.anim.slide_in_right,
						R.anim.slide_out_left,
						R.anim.slide_out_right)
				.add(R.id.file_picker_container, fragment)
				.addToBackStack(null)
				.commit()
	}

	override fun getPickedFiles(): Collection<File> {
		return pickedFiles
	}

	companion object {

		fun newInstance() = FilePickerFragment()
	}

	class FilePickerEntry : Fragment {

		private lateinit var recyclerView: RecyclerView
		private lateinit var adapter: FilePickerRecyclerAdapter

		private lateinit var file: File
		private lateinit var pickedStateProvider: PickedStateProvider
		private lateinit var fileClickedListener: OnFileClickedListener
		private lateinit var filePickedStateChangeListener: OnFilePickedStateChangeListener

		constructor() {

		}

		override fun onCreateView(inflater: LayoutInflater,
								  container: ViewGroup?,
								  savedInstanceState: Bundle?): View {

			return inflater.inflate(R.layout.fragment_file_picker, container, false)
		}

		override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
			recyclerView = view.getViewById(R.id.recycler_view)
			configureRecyclerView()
		}

		private fun configureRecyclerView() {
			adapter = FilePickerRecyclerAdapter.Builder()
					.setDirectory(file)
					.setPickedStateProvider(pickedStateProvider)
					.addFileClickedListener(fileClickedListener)
					.addFilePickedListener(filePickedStateChangeListener)
					.build()

			recyclerView.adapter = adapter
			recyclerView.layoutManager = LinearLayoutManager(context)
		}

		companion object {

			@JvmStatic
			fun newInstance(
					file: File,
					pickedStateProvider: PickedStateProvider,
					fileClickedListener: OnFileClickedListener,
					filePickedStateChangeListener: OnFilePickedStateChangeListener): FilePickerEntry {

				val fragment = FilePickerEntry()
				fragment.file = file
				fragment.pickedStateProvider = pickedStateProvider
				fragment.fileClickedListener = fileClickedListener
				fragment.filePickedStateChangeListener = filePickedStateChangeListener
				return fragment
			}
		}
	}
}