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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.irateam.vkplayer.R
import com.irateam.vkplayer.adapter.LocalAudioRecyclerViewAdapter
import com.irateam.vkplayer.api.SimpleProgressableCallback
import com.irateam.vkplayer.api.service.LocalAudioService
import com.irateam.vkplayer.controller.PlayerController
import com.irateam.vkplayer.dialog.LocalAudioRemoveAlertDialog
import com.irateam.vkplayer.event.AudioScannedEvent
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.models.LocalAudio
import com.irateam.vkplayer.player.Player
import com.irateam.vkplayer.util.Comparators
import com.irateam.vkplayer.util.EventBus
import com.irateam.vkplayer.util.extension.*
import java.util.*

class LocalAudioListFragment : Fragment(),
        ActionMode.Callback,
        SearchView.OnQueryTextListener,
        BackPressedListener,
        LocalAudioRecyclerViewAdapter.CheckedListener {

    private val adapter = LocalAudioRecyclerViewAdapter()

    /**
     * Services
     */
    private lateinit var localAudioService: LocalAudioService

    /**
     * Views
     */
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: View
    private lateinit var sortModeHolder: View
    private lateinit var scanProgressHolder: View
    private lateinit var scanProgress: TextView

    /**
     * Menus
     */
    private lateinit var menu: Menu
    private lateinit var searchView: SearchView

    /**
     * Action Mode
     */
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        localAudioService = LocalAudioService(context)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        return inflater.inflate(R.layout.fragment_local_audio_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.getViewById(R.id.recycler_view)
        configureRecyclerView()

        refreshLayout = view.getViewById(R.id.refresh_layout)
        configureRefreshLayout()

        sortModeHolder = view.getViewById(R.id.sort_mode_holder)
        configureSortModeHolder()

        emptyView = view.getViewById(R.id.empty_view)
        configureEmptyView()

        scanProgressHolder = view.getViewById(R.id.scan_progress_holder)
        scanProgress = view.getViewById(R.id.scan_progress)

        adapter.checkedListener = this

        EventBus.register(adapter)
        loadLocalAudios()
    }

    private fun configureRecyclerView() {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun configureRefreshLayout() {
        refreshLayout.setColorSchemeResources(R.color.accent, R.color.primary)
        refreshLayout.setOnRefreshListener {
            actionMode?.finish()
            if (adapter.isSortMode()) {
                commitSortMode()
            }
            loadLocalAudios()
        }
    }

    private fun configureSortModeHolder() {
        sortModeHolder.apply {
            findViewById(R.id.sort_by_title).setOnClickListener {
                adapter.sort(Comparators.TITLE_COMPARATOR)
            }

            findViewById(R.id.sort_by_artist).setOnClickListener {
                adapter.sort(Comparators.ARTIST_COMPARATOR)
            }

            findViewById(R.id.sort_by_length).setOnClickListener {
                adapter.sort(Comparators.ARTIST_REVERSE_COMPARATOR)
            }
        }
    }

    private fun configureEmptyView() {
        emptyView.findViewById(R.id.empty_list_scan_audio).setOnClickListener {
            scanLocalAudios()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.unregister(adapter)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        activity.menuInflater.inflate(R.menu.menu_local_audio_list, menu)
        val itemSearch = menu.findItem(R.id.action_search)

        searchView = itemSearch.actionView as SearchView
        searchView.setIconifiedByDefault(false)
        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String) = false

    override fun onQueryTextChange(query: String): Boolean {
        adapter.setSearchQuery(query)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_sort -> {
            startSortMode()
            true
        }

        R.id.action_sort_done -> {
            commitSortMode()
            true
        }

        R.id.action_scan -> {
            scanLocalAudios()
            true
        }

        else -> false
    }

    override fun onChanged(audio: Audio, checked: HashSet<LocalAudio>) {
        if (actionMode == null && checked.size > 0) {
            actionMode = activity.startActionMode(this)
        }

        actionMode?.apply {
            if (checked.isEmpty()) {
                finish()
                return
            }

            title = checked.size.toString()
        }

    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        actionMode = mode
        mode.menuInflater.inflate(R.menu.menu_local_audio_list_context, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_play -> {
                val audios = adapter.checkedAudios.toList()
                Player.play(audios, audios[0])
            }

            R.id.action_play_next -> {
                val audios = adapter.checkedAudios.toList()
                Player.addToPlayNext(audios)
            }

            R.id.action_delete -> {
                adapter.removeChecked()
            }

            R.id.action_add_to_queue -> {
                Player.addToQueue(adapter.checkedAudios)
            }

            R.id.action_remove_from_filesystem -> {
                val dialog = LocalAudioRemoveAlertDialog()
                dialog.positiveButtonClickListener = {
                    removeCheckedLocalAudios()
                    actionMode?.finish()
                }
                dialog.show(fragmentManager, LocalAudioRemoveAlertDialog.TAG)
                return false
            }
        }
        mode.finish()
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        adapter.clearChecked()
        actionMode = null
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {

        when (requestCode) {
            SCAN_LOCAL_AUDIO_REQUEST_CODE -> if (grantResults.isNotEmpty()
                    && grantResults.first() == PackageManager.PERMISSION_GRANTED) {

                startScanLocalAudios()
            } else {
                Toast.makeText(context, "We Need permission Storage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if (adapter.isSortMode()) {
            revertSortMode()
            return true
        } else {
            return false
        }
    }

    private fun startSortMode() {
        adapter.startSortMode()
        configureStartSortMode()
    }

    private fun commitSortMode() {
        adapter.commitSortMode()
        configureFinishSortMode()
    }

    private fun revertSortMode() {
        adapter.revertSortMode()
        configureFinishSortMode()
    }

    private fun configureStartSortMode() {
        activity.apply {
            if (this is PlayerController.VisibilityController) {
                hidePlayerController()
            }
        }

        sortModeHolder.slideInUp()
        menu.findItem(R.id.action_sort).isVisible = false
        menu.findItem(R.id.action_sort_done).isVisible = true
    }

    private fun configureFinishSortMode() {
        activity.apply {
            if (this is PlayerController.VisibilityController) {
                showPlayerController()
            }
        }

        sortModeHolder.slideOutDown()
        menu.findItem(R.id.action_sort).isVisible = true
        menu.findItem(R.id.action_sort_done).isVisible = false
    }

    private fun loadLocalAudios() {
        refreshLayout.post { refreshLayout.isRefreshing = true }
        localAudioService.getAllIndexed().execute(success<List<LocalAudio>> {
            adapter.setAudios(it)
            emptyView.isVisible = it.isEmpty()
        } finish {
            refreshLayout.post { refreshLayout.isRefreshing = false }
        })
    }

    private fun removeCheckedLocalAudios() {
        localAudioService.removeFromFilesystem(adapter.checkedAudios).execute(
                success<Collection<LocalAudio>> {
                    adapter.removeAll(it)
                })
    }

    private fun scanLocalAudios() {
        requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                SCAN_LOCAL_AUDIO_REQUEST_CODE)
    }

    private fun startScanLocalAudios() {
        scanProgressHolder.slideInDown()
        scanProgress.isVisible = false
        adapter.setAudios(emptyList())
        localAudioService.scan().execute(
                SimpleProgressableCallback<List<LocalAudio>, AudioScannedEvent> {
                    adapter.setAudios(it)
                } progress {
                    if (!scanProgress.isVisible) {
                        scanProgress.isVisible = true
                    }

                    if (emptyView.isVisible) {
                        emptyView.isVisible = false
                    }

                    adapter.addAudio(it.audio)
                    scanProgress.text = "${it.current}/${it.total}"
                } finish {
                    scanProgressHolder.slideOutUp()
                })
    }


    companion object {

        val TAG = LocalAudioListFragment::class.java.name
        val SCAN_LOCAL_AUDIO_REQUEST_CODE = 1

        @JvmStatic
        fun newInstance(): LocalAudioListFragment {
            return LocalAudioListFragment()
        }
    }
}