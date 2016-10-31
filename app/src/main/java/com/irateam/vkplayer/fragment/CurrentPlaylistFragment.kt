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
import com.irateam.vkplayer.adapter.CurrentPlaylistRecyclerAdapter
import com.irateam.vkplayer.util.EventBus
import com.irateam.vkplayer.util.extension.getViewById

class CurrentPlaylistFragment : Fragment() {

    private lateinit var playlist: RecyclerView
    private lateinit var adapter: CurrentPlaylistRecyclerAdapter

    private lateinit var emptyView: View

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_current_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        playlist = view.getViewById(R.id.playlist)
        configurePlaylist()

        emptyView = view.getViewById(R.id.empty_view)
        configureEmptyView()

        EventBus.register(adapter)
    }

    override fun onDestroy() {
        EventBus.unregister(adapter)
        super.onDestroy()
    }

    private fun configureEmptyView() {

    }

    private fun configurePlaylist() {
        adapter = CurrentPlaylistRecyclerAdapter()

        playlist.adapter = adapter
        playlist.layoutManager = LinearLayoutManager(context)
    }

    companion object {

        val TAG: String = CurrentPlaylistFragment::class.java.name

        fun newInstance() = CurrentPlaylistFragment()
    }
}