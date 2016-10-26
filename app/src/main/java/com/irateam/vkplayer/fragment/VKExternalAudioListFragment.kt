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

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.annotation.MenuRes
import android.view.*
import android.widget.TextView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.adapter.LocalAudioRecyclerAdapter
import com.irateam.vkplayer.api.service.VKExternalAudioService
import com.irateam.vkplayer.dialog.LocalAudioRemoveAlertDialog
import com.irateam.vkplayer.util.Permission
import com.irateam.vkplayer.util.extension.*

class VKExternalAudioListFragment : BaseAudioListFragment() {

	override val adapter = LocalAudioRecyclerAdapter()

	/**
	 * Services
	 */
	private lateinit var audioService: VKExternalAudioService

	/**
	 * Views
	 */
	private lateinit var scanProgressHolder: View
	private lateinit var scanProgress: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		audioService = VKExternalAudioService(context)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {

		return inflater.inflate(R.layout.fragment_local_audio_list, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		scanProgressHolder = view.getViewById(R.id.scan_progress_holder)
		scanProgress = view.getViewById(R.id.scan_progress)

		adapter.checkedListener = this

		loadLocalAudios()
	}

	@MenuRes
	override fun getMenuResource(): Int {
		return R.menu.menu_local_audio_list
	}

	override fun onRefresh() {
		loadLocalAudios()
	}

	override fun getActionModeMenuResource(): Int {
		return R.menu.menu_local_audio_list_context
	}

	override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_remove_from_filesystem -> {
				val dialog = LocalAudioRemoveAlertDialog()
				dialog.positiveButtonClickListener = { removeCheckedLocalAudios() }
				dialog.show(fragmentManager, LocalAudioRemoveAlertDialog.TAG)
				return false
			}
		}

		return super.onActionItemClicked(mode, item)
	}

	private fun loadLocalAudios() {
		refreshLayout.post { refreshLayout.isRefreshing = true }
		audioService.getExternal().execute {
			onSuccess {
				adapter.setAudios(it)
				emptyView.isVisible = it.isEmpty()
			}

			onFinish {
				refreshLayout.post { refreshLayout.isRefreshing = false }
			}
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int,
											permissions: Array<out String>,
											grantResults: IntArray) {

		when (requestCode) {
			SCAN_LOCAL_AUDIO_REQUEST_CODE    -> if (grantResults.isNotEmpty()
					&& grantResults.first() == PackageManager.PERMISSION_GRANTED) {

				loadLocalAudios()
			} else {
				showLongToast(R.string.permission_required_to_scan)
			}

			REMOVE_CHECKED_LOCAL_AUDIOS_CODE -> if (grantResults.isNotEmpty()
					&& grantResults.first() == PackageManager.PERMISSION_GRANTED) {

				startRemoveCheckedLocalAudios()
			} else {
				showLongToast(R.string.permission_required_to_remove)
			}
		}
	}

	private fun removeCheckedLocalAudios() {
		if (isPermissionsGranted(Permission.WRITE_EXTERNAL_STORAGE)) {
			startRemoveCheckedLocalAudios()
		} else {
			requestPermissions(REMOVE_CHECKED_LOCAL_AUDIOS_CODE,
					Permission.WRITE_EXTERNAL_STORAGE)
		}

	}

	private fun startRemoveCheckedLocalAudios() {
		audioService.removeFromFilesystem(adapter.checkedAudios).execute {
			onSuccess {
				adapter.removeAll(it)
			}

			onError {
				showLongToast("Error occurred!")
			}
		}
		actionMode?.finish()
	}

	companion object {

		val TAG: String = LocalAudioListFragment::class.java.name
		val SCAN_LOCAL_AUDIO_REQUEST_CODE = 1
		val REMOVE_CHECKED_LOCAL_AUDIOS_CODE = 2

		@JvmStatic
		fun newInstance() = VKExternalAudioListFragment()
	}
}