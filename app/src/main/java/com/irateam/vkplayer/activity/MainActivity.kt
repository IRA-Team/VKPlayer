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
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.activity.settings.SettingsActivity
import com.irateam.vkplayer.api.SimpleCallback
import com.irateam.vkplayer.api.service.UserService
import com.irateam.vkplayer.api.service.VKAudioService
import com.irateam.vkplayer.controller.PlayerController
import com.irateam.vkplayer.fragment.AudioListFragment
import com.irateam.vkplayer.models.User
import com.irateam.vkplayer.service.PlayerService
import com.irateam.vkplayer.util.EventBus
import com.irateam.vkplayer.util.extension.getViewById
import com.irateam.vkplayer.util.extension.setRoundImageURL
import com.melnykov.fab.FloatingActionButton
import com.vk.sdk.VKSdk

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    //Services
    private lateinit var userService: UserService
    private lateinit var audioService: VKAudioService

    //Views
    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var fab: FloatingActionButton

    //User views
    private lateinit var userPhoto: ImageView
    private lateinit var userFullName: TextView
    private lateinit var userVkLink: TextView

    //Player helpers
    private lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = getViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { v -> drawerLayout.openDrawer(GravityCompat.START) }

        drawerLayout = getViewById(R.id.drawer_layout)
        val drawerToggle = ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawerLayout.setDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigationView = getViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

        val header = navigationView.getHeaderView(0)
        userPhoto = header.getViewById(R.id.user_photo)
        userFullName = header.getViewById(R.id.user_full_name)
        userVkLink = header.getViewById(R.id.user_vk_link)

        coordinatorLayout = getViewById(R.id.coordinator_layout)

        fab = getViewById(R.id.fab)
        fab.setOnClickListener { startActivity(Intent(this, AudioActivity::class.java)) }

        playerController = PlayerController(this, findViewById(R.id.player_panel)!!)
        playerController.initialize()

        audioService = VKAudioService(this)
        userService = UserService(this)

        EventBus.register(playerController)
        startService(Intent(this, PlayerService::class.java))

        initializeUser()
        initializeFragment()
    }

    override fun onStop() {
        super.onStop()
        EventBus.unregister(this)
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

    private fun initializeUser() {
        userService.getCurrent().execute(SimpleCallback { setUser(it) })
    }

    private fun setUser(user: User) {
        userPhoto.setRoundImageURL(user.photo100px)
        userFullName.text = user.fullName
        userVkLink.text = "http://vk.com/id" + user.id
    }

    private fun initializeFragment() {
        val item = navigationView.menu.findItem(R.id.my_audio)
        item.isChecked = true
        onNavigationItemSelected(item)
    }

    fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    private fun VkLogout() {
        VKSdk.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
