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

package com.irateam.vkplayer.activity.settings

import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import com.irateam.vkplayer.R
import com.irateam.vkplayer.api.service.SettingsService
import com.irateam.vkplayer.api.service.VKAudioService
import com.irateam.vkplayer.service.DownloadService

class SettingsActivity : AppCompatPreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var settingsService: SettingsService? = null

    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsService = SettingsService(this)

        val root = findViewById(android.R.id.list).parent.parent.parent as LinearLayout
        toolbar = LayoutInflater.from(this).inflate(R.layout.content_toolbar, root, false) as Toolbar
        toolbar!!.title = resources.getString(R.string.title_activity_settings)
        root.addView(toolbar, 0)
        toolbar!!.setNavigationOnClickListener { v -> finish() }
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || SyncPreferenceFragment::class.java.name == fragmentName
                || CachePreferenceFragment::class.java.name == fragmentName
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == SettingsService.SYNC_TIME) {
            settingsService!!.setSyncAlarm()
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class SyncPreferenceFragment : PreferenceFragment() {

        private var settingsService: SettingsService? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            settingsService = SettingsService(activity)
            addPreferencesFromResource(R.xml.pref_sync)
            setHasOptionsMenu(true)

            bindPreferenceSummaryToValue(findPreference("sync_time"))
            bindPreferenceSummaryToValue(findPreference("sync_count"))
            bindPreferenceSummaryToValue(findPreference("sync_button"))

            findPreference("sync_button").setOnPreferenceClickListener { preference ->
                val intent = DownloadService.startSyncIntent(activity, true)
                activity.startService(intent)
                false
            }

            findPreference("sync_enabled").setOnPreferenceChangeListener { preference, newValue ->
                val syncEnabled = newValue as Boolean
                if (syncEnabled) {
                    settingsService!!.setSyncAlarm()
                } else {
                    settingsService!!.cancelSyncAlarm()
                }
                true
            }
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class CachePreferenceFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_cache)
            setHasOptionsMenu(true)

            bindPreferenceSummaryToValue(findPreference("cache_clear"))

            findPreference("cache_clear").setOnPreferenceClickListener { preference ->
                val builder = AlertDialog.Builder(activity)
                builder.setTitle(getString(R.string.clear_cache_dialog_title)).setMessage(getString(R.string.clear_cache_dialog_text)).setPositiveButton(getString(R.string.yes)) { dialog, id ->
                    VKAudioService(activity).removeAllCachedAudio()
                    Toast.makeText(activity, getString(R.string.cache_clear_complete), Toast.LENGTH_SHORT).show()
                }.setNegativeButton(getString(R.string.no)) { dialog, id ->

                }
                builder.create().show()
                false
            }


        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }


        private val sBindPreferenceSummaryToValueListener: Preference.OnPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference, value: Any ->
                    preference.summary = value.toString()
                    true
                }

        private fun bindPreferenceSummaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
        }
    }

}
