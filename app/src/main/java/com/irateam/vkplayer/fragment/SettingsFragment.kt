package com.irateam.vkplayer.fragment

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.view.View
import com.irateam.vkplayer.R
import com.irateam.vkplayer.api.service.SettingsService
import com.irateam.vkplayer.dialog.LanguagePickerDialog
import com.irateam.vkplayer.dialog.NumberPickerDialog
import com.irateam.vkplayer.ui.Preference
import com.irateam.vkplayer.ui.PreferenceSwitch
import com.irateam.vkplayer.util.Languages
import com.irateam.vkplayer.util.extension.calendarOf
import com.irateam.vkplayer.util.extension.getViewById
import com.irateam.vkplayer.util.extension.toTime
import com.irateam.vkplayer.util.extension.toTimeFormatted
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog


class SettingsFragment : BaseFragment() {

    private lateinit var settingsService: SettingsService

    private lateinit var syncEnabled: PreferenceSwitch
    private lateinit var syncWifiOnly: PreferenceSwitch
    private lateinit var syncTime: Preference
    private lateinit var syncCount: Preference

    private lateinit var language: Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsService = SettingsService(context)
    }

    @StringRes
    override fun getTitleRes(): Int {
        return R.string.navigation_drawer_settings
    }

    @LayoutRes
    override fun getLayoutRes(): Int {
        return R.layout.fragment_settings
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeSyncEnabled()
        initializeSyncWifiOnly()
        initializeSyncTime()
        initializeSyncCount()
        setSyncEnabled(settingsService.syncEnabled)

        initializeLanguage()
    }

    //Sync
    private fun initializeSyncEnabled() {
        syncEnabled = getViewById(R.id.sync_enabled)
        syncEnabled.setOnCheckedChangeListener { enabled -> setSyncEnabled(enabled) }
    }

    private fun initializeSyncWifiOnly() {
        syncWifiOnly = getViewById(R.id.sync_wifi_only)
        syncWifiOnly.isChecked = settingsService.syncWifiOnly
        syncWifiOnly.setOnCheckedChangeListener { enabled ->
            settingsService.syncWifiOnly = enabled
        }
    }

    private fun initializeSyncTime() {
        syncTime = getViewById(R.id.sync_time)
        syncTime.subtitle = settingsService.syncTime.toTimeFormatted()
        syncTime.setOnClickListener {
            val (hours, minutes, seconds) = settingsService.syncTime.toTime()
            val onTimePicked = TimePickerDialog.OnTimeSetListener { v, hour, minute, second ->
                val time = calendarOf(hour, minute, second)
                settingsService.syncTime = time
                syncTime.subtitle = time.toTimeFormatted()
            }
            val picker = TimePickerDialog.newInstance(onTimePicked, hours, minutes, seconds, false)
            picker.show(activity.fragmentManager, "TimePickerDialog")
        }
    }

    private fun initializeSyncCount() {
        syncCount = getViewById(R.id.sync_count)
        syncCount.subtitle = settingsService.syncCount.toString()
        syncCount.setOnClickListener {
            val picker = NumberPickerDialog.newInstance(settingsService.syncCount)
            picker.onNumberPickedListener = { count ->
                settingsService.syncCount = count
                syncCount.subtitle = count.toString()
            }
            picker.show(activity.supportFragmentManager, NumberPickerDialog.TAG)
        }
    }

    private fun setSyncEnabled(enabled: Boolean) {
        syncWifiOnly.isEnabled = enabled
        syncTime.isEnabled = enabled
    }

    //Other
    private fun initializeLanguage() {
        language = getViewById(R.id.language)
        Languages.getLanguageByCode(context, settingsService.language)?.let {
            language.subtitle = it.name
        }
        language.setOnClickListener {
            val picker = LanguagePickerDialog.newInstance()
            picker.onLanguagePickedListener = { language ->
                settingsService.language = language.code
                picker.dismiss()
                activity.recreate()
            }
            picker.show(activity.supportFragmentManager, LanguagePickerDialog.TAG)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = SettingsFragment()
    }
}