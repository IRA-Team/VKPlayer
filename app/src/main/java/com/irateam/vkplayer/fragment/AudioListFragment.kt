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
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import com.irateam.vkplayer.R
import com.irateam.vkplayer.adapters.AudioRecyclerViewAdapter
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.api.SimpleCallback
import com.irateam.vkplayer.api.service.AudioService
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.ui.CustomItemAnimator
import com.irateam.vkplayer.utils.isVisible
import org.greenrobot.eventbus.EventBus

/**
 * @author Artem Glugovsky
 */
class AudioListFragment : Fragment(), ActionMode.Callback, SearchView.OnQueryTextListener {

    private val eventBus = EventBus.getDefault()
    private val adapter = AudioRecyclerViewAdapter()

    private lateinit var audioService: AudioService
    private lateinit var query: Query<List<Audio>>
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: View
    private lateinit var menu: Menu
    private lateinit var searchView: SearchView

    private var previousSearchQuery: Query<List<Audio>>? = null
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        return inflater.inflate(R.layout.fragment_audio_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recycler_view) as RecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = CustomItemAnimator()

        refreshLayout = view.findViewById(R.id.refresh_layout) as SwipeRefreshLayout
        refreshLayout.setColorSchemeResources(
                R.color.accent,
                R.color.primary)
        refreshLayout.setOnRefreshListener {
            actionMode?.finish()
            if (adapter.isSortMode()) {
                adapter.setSortMode(false)
            }
            executeQuery()
        }

        emptyView = view.findViewById(R.id.empty_view)

        adapter.checkedListener = { audio, checked ->
            if (actionMode == null && checked.size > 0) {
                actionMode = activity.startActionMode(this)
            }
            actionMode?.title = checked.size.toString()

            if (actionMode != null && checked.isEmpty()) {
                actionMode?.finish()
            }
        }

        audioService = AudioService(context)
        eventBus.register(adapter)
        executeQuery()
    }

    override fun onStop() {
        super.onStop()
        eventBus.unregister(adapter)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        val itemSearch = menu.findItem(R.id.action_search)

        searchView = itemSearch.actionView as SearchView
        searchView.setIconifiedByDefault(false)
        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String) = false

    override fun onQueryTextChange(query: String): Boolean {
        adapter.setSearchQuery(query)
        previousSearchQuery?.cancel()
        previousSearchQuery = audioService.search(query)
        previousSearchQuery?.execute(SimpleCallback.success { adapter.setSearchAudios(it) })

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
        else -> false
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        actionMode = mode
        mode.menuInflater.inflate(R.menu.menu_list_context, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    //TODO: Implement
    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_play -> false
        R.id.action_cache -> false
        R.id.action_remove_from_cache -> false
        R.id.action_delete -> false
        R.id.action_add_to_playlist -> false
        else -> false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        adapter.clearChecked()
        actionMode = null
    }

    private fun executeQuery() {
        refreshLayout.post { refreshLayout.isRefreshing = true }
        query.execute(SimpleCallback
                .success { audios: List<Audio> ->
                    adapter.setAudios(audios)
                    emptyView.isVisible = audios.isEmpty()
                }
                .finish { refreshLayout.post { refreshLayout.isRefreshing = false } })
    }


    companion object {

        @JvmStatic
        fun newInstance(query: Query<List<Audio>>): AudioListFragment {
            val fragment = AudioListFragment()
            fragment.query = query
            return fragment
        }
    }

}
