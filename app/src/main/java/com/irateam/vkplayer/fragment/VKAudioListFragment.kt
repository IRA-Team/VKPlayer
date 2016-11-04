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
import android.support.annotation.LayoutRes
import android.support.annotation.MenuRes
import android.support.annotation.StringRes
import android.support.v7.widget.SearchView
import android.view.ActionMode
import android.view.MenuItem
import android.view.View
import com.irateam.vkplayer.R
import com.irateam.vkplayer.adapter.VKAudioRecyclerAdapter
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.api.service.VKAudioService
import com.irateam.vkplayer.model.Audio
import com.irateam.vkplayer.model.VKAudio
import com.irateam.vkplayer.service.DownloadService
import com.irateam.vkplayer.util.extension.execute
import com.irateam.vkplayer.util.extension.isVisible
import com.irateam.vkplayer.util.extension.showLongToast
import java.util.*

/**
 * @author Artem Glugovsky
 */
abstract class VKAudioListFragment : BaseAudioListFragment(),
        ActionMode.Callback,
        SearchView.OnQueryTextListener {

    override val adapter = VKAudioRecyclerAdapter()

    protected abstract var query: Query<List<VKAudio>>
    protected lateinit var audioService: VKAudioService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        audioService = VKAudioService(context)
    }

    @LayoutRes
    override fun getLayoutRes(): Int {
        return R.layout.fragment_vk_audio_list
    }

    @MenuRes
    override fun getMenuResource(): Int {
        return R.menu.menu_vk_audio_list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.checkedListener = this
        loadVKAudios()
    }

    override fun onRefresh() {
        loadVKAudios()
    }

    override fun onQueryTextChange(query: String): Boolean {
        super.onQueryTextChange(query)
        adapter.setSearchQuery(query)
        return true
    }

    override fun onChanged(audio: Audio, checked: HashSet<out Audio>) {
        super.onChanged(audio, checked)
        actionMode?.apply {
            val itemCache = menu.findItem(R.id.action_cache)
            itemCache.isVisible = checked
                    .filterIsInstance<VKAudio>()
                    .filter { !it.isCached }
                    .isNotEmpty()

            val itemRemoveFromCache = menu.findItem(R.id.action_remove_from_cache)
            itemRemoveFromCache.isVisible = checked
                    .filterIsInstance<VKAudio>()
                    .filter { it.isCached }
                    .isNotEmpty()
        }

    }

    override fun getActionModeMenuResource(): Int {
        return R.menu.menu_vk_audio_list_context
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_cache -> {
                val nonCached = adapter.checkedAudios.filter { !it.isCached }
                DownloadService.download(context, nonCached)
                actionMode?.finish()
                return true
            }

            R.id.action_remove_from_cache -> {
                val cached = adapter.checkedAudios.filter { it.isCached }
                audioService.removeFromCache(cached).execute {
                    onSuccess {
                        adapter.removeChecked()
                        adapter.removeFromCache(it)
                    }
                }
                return true
            }
        }

        return super.onActionItemClicked(mode, item)
    }

    private fun loadVKAudios() {
        refreshLayout.post { refreshLayout.isRefreshing = true }
        query.execute {
            onSuccess {
                adapter.audios = it
                emptyView.isVisible = it.isEmpty()
                adapter.notifyDataSetChanged()
            }
            onError {
                showLongToast(R.string.error_load)
            }
            onFinish {
                refreshLayout.post { refreshLayout.isRefreshing = false }
            }
        }
    }
}
