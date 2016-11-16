package com.irateam.vkplayer.fragment

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.view.View
import com.irateam.vkplayer.R
import com.irateam.vkplayer.api.service.SettingsService
import com.irateam.vkplayer.dialog.LanguagePickerDialog
import com.irateam.vkplayer.ui.Preference
import com.irateam.vkplayer.ui.PreferenceSwitch
import com.irateam.vkplayer.util.Languages
import com.irateam.vkplayer.util.extension.calendarOf
import com.irateam.vkplayer.util.extension.getViewById
import com.irateam.vkplayer.util.extension.toTime
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog


class SettingsFragment : BaseFragment() {

    private lateinit var settingsService: SettingsService

    private lateinit var syncEnabled: PreferenceSwitch
    private lateinit var syncTime: Preference

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

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        syncEnabled = getViewById(R.id.sync_enabled)
        syncEnabled.assignToPreferences(settingsService, SettingsService::syncEnabled)

        syncTime = getViewById(R.id.sync_time)
        syncTime.setOnClickListener {
            val (hours, minutes, seconds) = settingsService.syncTime.toTime()
            val onTimePicked = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute, second ->
                settingsService.syncTime = calendarOf(
                        hours = hourOfDay,
                        minutes = minute,
                        seconds = second)
            }
            TimePickerDialog.newInstance(onTimePicked, hours, minutes, seconds, false)
        }

        language = getViewById(R.id.language)
        Languages.getLanguageByCode(context, settingsService.language)?.let {
            language.subtitle = it.name
        }
        language.setOnClickListener {
            val picker = LanguagePickerDialog()
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