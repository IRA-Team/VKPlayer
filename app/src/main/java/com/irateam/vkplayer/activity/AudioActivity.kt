/*
 * Copyright (C) 2015 IRA-Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irateam.vkplayer.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.irateam.vkplayer.R
import com.irateam.vkplayer.api.service.VKAudioService
import com.irateam.vkplayer.controller.ActivityPlayerController
import com.irateam.vkplayer.controller.PlayerController
import com.irateam.vkplayer.event.DownloadFinishedEvent
import com.irateam.vkplayer.model.VKAudio
import com.irateam.vkplayer.player.Player
import com.irateam.vkplayer.service.DownloadService
import com.irateam.vkplayer.service.PlayerService
import com.irateam.vkplayer.util.EventBus
import com.irateam.vkplayer.util.extension.execute
import com.irateam.vkplayer.util.extension.getViewById
import com.irateam.vkplayer.util.extension.startService
import com.melnykov.fab.FloatingActionButton
import org.greenrobot.eventbus.Subscribe

class AudioActivity : AppCompatActivity() {

    private val audioService = VKAudioService(this)

    private lateinit var playerController: PlayerController
    private lateinit var menu: Menu
    private lateinit var toolbar: Toolbar
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)

        toolbar = getViewById(R.id.toolbar_transparent)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        fab = getViewById(R.id.fab)
        fab.setOnClickListener { finish() }

        playerController = ActivityPlayerController(this, findViewById(R.id.activity_player_panel)!!)
        playerController.initialize()

        startService<PlayerService>()
        EventBus.register(this)
        EventBus.register(playerController)
    }

    override fun onDestroy() {
        EventBus.unregister(this)
        EventBus.unregister(playerController)
        super.onDestroy()
    }

    @Subscribe
    fun onDownloadFinished(e: DownloadFinishedEvent) {
        val audio = e.audio
        when (audio) {
            is VKAudio -> setCacheAction(audio.isCached)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_audio, menu)
        val audio = Player.audio
        when (audio) {
            is VKAudio -> setCacheAction(audio.isCached)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }

        R.id.action_cache -> {
            saveCurrentAudioToCache()
            true
        }

        R.id.action_remove_from_cache -> {
            removeCurrentAudioFromCache()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun saveCurrentAudioToCache() {
        Player.audio?.let {
            DownloadService.download(this, listOf(it))
        }
    }

    private fun removeCurrentAudioFromCache() {
        val audio = Player.audio
        if (audio is VKAudio) {
            audioService.removeFromCache(listOf(audio)).execute {
                onSuccess {
                    setCacheAction(false)
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_out_up_close, R.anim.slide_out_down)
    }

    fun setCacheAction(isCached: Boolean) = if (isCached) {
        menu.findItem(R.id.action_remove_from_cache).isVisible = true
        menu.findItem(R.id.action_cache).isVisible = false
    } else {
        menu.findItem(R.id.action_remove_from_cache).isVisible = false
        menu.findItem(R.id.action_cache).isVisible = true
    }
}
