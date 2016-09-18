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
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.TextView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.adapter.LocalAudioRecyclerViewAdapter
import com.irateam.vkplayer.api.SimpleProgressableCallback
import com.irateam.vkplayer.api.service.LocalAudioService
import com.irateam.vkplayer.dialog.LocalAudioRemoveAlertDialog
import com.irateam.vkplayer.event.AudioScannedEvent
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.models.LocalAudio
import com.irateam.vkplayer.player.Player
import com.irateam.vkplayer.util.EventBus
import com.irateam.vkplayer.util.extension.getViewById
import com.irateam.vkplayer.util.extension.isVisible
import com.irateam.vkplayer.util.extension.success
import java.util.*

class LocalAudioListFragment : Fragment(),
        ActionMode.Callback,
        SearchView.OnQueryTextListener,
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
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()

        refreshLayout = view.getViewById(R.id.refresh_layout)
        refreshLayout.setColorSchemeResources(R.color.accent, R.color.primary)
        refreshLayout.setOnRefreshListener {
            actionMode?.finish()
            if (adapter.isSortMode()) {
                adapter.setSortMode(false)
            }
            loadLocalAudios()
        }

        emptyView = view.findViewById(R.id.empty_view)
        scanProgressHolder = view.getViewById(R.id.scan_progress_holder)
        scanProgress = view.getViewById(R.id.scan_progress)

        adapter.checkedListener = this

        EventBus.register(adapter)
        loadLocalAudios()
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
            adapter.setSortMode(true)
            item.isVisible = false
            menu.findItem(R.id.action_sort_done).isVisible = true
            true
        }

        R.id.action_sort_done -> {
            adapter.setSortMode(false)
            item.isVisible = false
            menu.findItem(R.id.action_sort).isVisible = true
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

        if (actionMode != null && checked.isEmpty()) {
            actionMode?.finish()
            return
        }

        val actionMode = actionMode
        if (actionMode != null) {
            actionMode.title = checked.size.toString()
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
        scanProgressHolder.isVisible = true
        scanProgress.isVisible = false
        adapter.setAudios(emptyList())
        localAudioService.scan().execute(
                SimpleProgressableCallback<List<LocalAudio>, AudioScannedEvent> {
                    adapter.setAudios(it)
                } progress {
                    if (!scanProgress.isVisible) {
                        scanProgress.isVisible = true
                    }
                    adapter.addAudio(it.audio)
                    scanProgress.text = "${it.current}/${it.total}"
                } finish {
                    scanProgressHolder.isVisible = false
                })
    }

    companion object {

        val TAG = LocalAudioListFragment::class.java.name

        @JvmStatic
        fun newInstance(): LocalAudioListFragment {
            return LocalAudioListFragment()
        }
    }
}