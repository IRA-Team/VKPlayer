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

package com.irateam.vkplayer.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.irateam.vkplayer.R
import com.irateam.vkplayer.activities.settings.SettingsActivity
import com.irateam.vkplayer.api.service.AudioService
import com.irateam.vkplayer.controllers.PlayerController
import com.irateam.vkplayer.fragment.AudioListFragment
import com.irateam.vkplayer.services.PlayerService
import com.vk.sdk.VKSdk
import org.greenrobot.eventbus.EventBus

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val eventBus = EventBus.getDefault()
    private val audioService = AudioService(this)

    //Views
    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var coordinatorLayout: CoordinatorLayout

    //Player helpers
    private lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { v -> drawerLayout.openDrawer(GravityCompat.START) }

        drawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
        val drawerToggle = ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawerLayout.setDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigationView = findViewById(R.id.navigation_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        coordinatorLayout = findViewById(R.id.coordinator_layout) as CoordinatorLayout

        playerController = PlayerController(this, findViewById(R.id.player_panel)!!)
        playerController.initialize()
        val listener = View.OnClickListener { startActivity(Intent(this, AudioActivity::class.java)) }
        playerController.setFabOnClickListener(listener)

        eventBus.register(playerController)
        startService(Intent(this, PlayerService::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return true
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        drawerLayout.closeDrawers()

        val itemId = menuItem.itemId
        val groupId = menuItem.groupId

        if (groupId == R.id.audio_group) {
            supportActionBar?.title = menuItem.title
            // @formatter:off
            val query = when (itemId) {
                R.id.current_playlist  -> audioService.getCurrent()
                R.id.my_audio          -> audioService.getMy()
                R.id.recommended_audio -> audioService.getRecommendation()
                R.id.popular_audio     -> audioService.getPopular()
                R.id.cached_audio      -> audioService.getCached()
                else                   -> audioService.getCurrent()
            }
            // @formatter:on
            val fragment = AudioListFragment.newInstance(query)
            showFragment(fragment)
            return true

        } else if (groupId == R.id.secondary_group) {
            when (itemId) {
                R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.exit -> VkLogout()
            }
            return true
        }

        return false
    }

    fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    override fun onBackPressed() {
        /* TODO: if (audioAdapter.isSortMode()) {
            audioAdapter.setSortMode(false);
            listView.setDragEnabled(false);
            refreshLayout.setEnabled(true);
            return;
        }*/
        super.onBackPressed()
    }

    private fun VkLogout() {
        VKSdk.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    fun performCheck(position: Int) {
        /*TODO: audioAdapter.toggleChecked(position);
        if (audioAdapter.getCheckedCount() > 0) {
            if (actionMode == null) {
                startActionMode(this);
            }
            actionMode.setTitle(String.valueOf(audioAdapter.getCheckedCount()));

            Menu menu = actionMode.getMenu();
            MenuItem cacheItem = menu.findItem(R.id.action_cache);
            MenuItem removeFromCache = menu.findItem(R.id.action_remove_from_cache);
            if (audioAdapter.getCachedCheckedItems().size() > 0) {
                removeFromCache.setVisible(true);
            } else {
                removeFromCache.setVisible(false);
            }

            if (audioAdapter.getNotCachedItems().size() > 0) {
                cacheItem.setVisible(true);
            } else {
                cacheItem.setVisible(false);
            }

        } else {
            actionMode.finish();
        }*/
    }

    fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        val menuItem = navigationView.menu.findItem(R.id.current_playlist)
        /*TODO: switch (item.getItemId()) {
            case R.id.action_play:
                playerService.setPlaylist(audioAdapter.getCheckedItems());
                playerService.play(0);
                menuItem.setChecked(true);
                onNavigationItemSelected(menuItem);
                break;

            case R.id.action_cache:
                Intent intent = new Intent(this, DownloadService.class);
                intent.setAction(DownloadService.START_DOWNLOADING);
                intent.putExtra(DownloadService.AUDIO_LIST, (ArrayList<Audio>) audioAdapter.getCheckedItems());
                startService(intent);
                break;

            case R.id.action_remove_from_cache:
                List<Audio> list = audioAdapter.getCachedCheckedItems();
                audioService.removeFromCache(list).execute(SimpleCallback.of(audioAdapter::updateAudiosById));
                break;

            case R.id.action_delete:
                audioAdapter.removeChecked();
                break;

            case R.id.action_add_to_playlist:
                List<Audio> checked = audioAdapter.getCheckedItems();
                List<Audio> playlist = playerService.getPlaylist();
                List<Audio> listToAdd = new ArrayList<>();
                for (Audio audio : checked) {
                    listToAdd.add(audio.clone());
                }
                playlist.addAll(0, listToAdd);
                audioAdapter.notifyDataSetChanged();
                Snackbar.make(coordinatorLayout, R.string.snackbar_add_to_playlist, Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_cancel, (v -> {
                            for (int i = 0; i < checked.size(); i++)
                                playlist.remove(0);
                            audioAdapter.notifyDataSetChanged();
                        }))
                        .show();
                break;
        }
        mode.finish();*/
        return true
    }
}
