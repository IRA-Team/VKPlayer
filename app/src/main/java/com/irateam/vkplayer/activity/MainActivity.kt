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

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.activity.settings.SettingsActivity
import com.irateam.vkplayer.api.service.LocalAudioService
import com.irateam.vkplayer.api.service.UserService
import com.irateam.vkplayer.api.service.VKAudioService
import com.irateam.vkplayer.controller.PlayerController
import com.irateam.vkplayer.fragment.*
import com.irateam.vkplayer.model.User
import com.irateam.vkplayer.player.PlayerPlayEvent
import com.irateam.vkplayer.player.PlayerStopEvent
import com.irateam.vkplayer.service.PlayerService
import com.irateam.vkplayer.util.EventBus
import com.irateam.vkplayer.util.extension.*
import com.melnykov.fab.FloatingActionButton
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKRequest
import org.greenrobot.eventbus.Subscribe

class MainActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        PlayerController.VisibilityController {

    //Services
    private lateinit var userService: UserService
    private lateinit var audioService: VKAudioService
    private lateinit var localAudioService: LocalAudioService

    //Views
    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var container: FrameLayout
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var fab: FloatingActionButton

    //User views
    private lateinit var userPhoto: ImageView
    private lateinit var userFullName: TextView
    private lateinit var userLink: TextView

    //Player helpers
    private lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioService = VKAudioService(this)
        localAudioService = LocalAudioService(this)
        userService = UserService(this)

        toolbar = getViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        drawerLayout = getViewById(R.id.drawer_layout)
        val drawerToggle = ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        navigationView = getViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

        val header = navigationView.getHeaderView(0)
        userPhoto = header.getViewById(R.id.user_photo)
        userFullName = header.getViewById(R.id.user_full_name)
        userLink = header.getViewById(R.id.user_vk_link)

        container = getViewById(R.id.container)
        coordinatorLayout = getViewById(R.id.coordinator_layout)

        fab = getViewById(R.id.fab)
        fab.setOnClickListener { startActivity(Intent(this, AudioActivity::class.java)) }

        playerController = PlayerController(this, findViewById(R.id.player_panel)!!)
        playerController.initialize()

        startService<PlayerService>()

        initializeUser()
        initializeFragment()

        EventBus.register(this)
        EventBus.register(playerController)
    }

    override fun onDestroy() {
        EventBus.unregister(this)
        EventBus.unregister(playerController)
        super.onDestroy()
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        drawerLayout.closeDrawers()

        val itemId = menuItem.itemId
        val groupId = menuItem.groupId

        if (groupId == R.id.audio_group) {
            val fragment: BaseFragment = when (itemId) {
                R.id.current_playlist -> CurrentPlaylistFragment.newInstance()
                R.id.local_audio -> LocalAudioListFragment.newInstance()
                R.id.my_audio -> VKMyAudioFragment.newInstance()
                R.id.recommended_audio -> VKRecommendationAudioFragment.newInstance()
                R.id.popular_audio -> VKPopularAudioFragment.newInstance()
                R.id.cached_audio -> VKCachedAudioFragment.newInstance()
                else -> throw IllegalStateException("This item doesn't support.")
            }
            setFragment(fragment)
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

    @Subscribe
    fun onPlayEvent(e: PlayerPlayEvent) {
        if (!playerController.isVisible()) {
            showPlayerController()
        }

        val fragment = supportFragmentManager.findFragmentByTag(TOP_LEVEL_FRAGMENT)
        if (fragment !is CurrentPlaylistFragment) {
            navigationView.menu.findItem(R.id.current_playlist)?.isChecked = true
            setFragment(CurrentPlaylistFragment.newInstance())
        }
    }

    @Subscribe
    fun onStopEvent(e: PlayerStopEvent) {
        hidePlayerController()
    }

    override fun showPlayerController() {
        container.layoutParams.apply {
            if (this is RelativeLayout.LayoutParams) {
                addRule(RelativeLayout.ABOVE, 0)
                playerController.show { addRule(RelativeLayout.ABOVE, R.id.player_panel) }
            } else {
                playerController.show()
            }
        }
    }

    override fun hidePlayerController() {
        playerController.hide()
    }

    private fun initializeUser() {
        userService.getCurrentCached().execute {
            onSuccess {
                setUser(it)
            }
            onError {

            }
            onFinish {
                loadUser()
            }
        }
    }

    private fun loadUser() {
        userService.getCurrent().execute {
            onSuccess {
                setUser(it)
            }
        }
    }

    private fun setUser(user: User) {
        userPhoto.setRoundImageURL(user.photo100px)
        userFullName.text = user.fullName
        userLink.text = "http://vk.com/id${user.id}"
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag(TOP_LEVEL_FRAGMENT)
        fragment?.let {
            if (it !is BackPressedListener || !it.onBackPressed()) {
                super.onBackPressed()
            }
        }
    }

    private fun initializeFragment() {
        val item = navigationView.menu.findItem(R.id.local_audio)
        item.isChecked = true
        //onNavigationItemSelected(item)
        setFragment(SettingsFragment.newInstance())
    }

    fun setFragment(fragment: BaseFragment) {
        supportActionBar?.title = getString(fragment.getTitleRes())
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment, TOP_LEVEL_FRAGMENT)
                .commit()
    }

    private fun VkLogout() {
        VKSdk.logout()
        startActivity<LoginActivity>()
        finish()
    }

    companion object {

        val TOP_LEVEL_FRAGMENT = "top_level_fragment"
    }
}
