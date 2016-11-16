package com.irateam.vkplayer.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.irateam.vkplayer.api.service.SettingsService
import java.util.*

abstract class BaseActivity : AppCompatActivity() {

    private var settings: SettingsService? = null

    protected val settingsService: SettingsService
        get() = settings ?: throw IllegalStateException(
                "SettingsService must be called after onCreate!")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings = SettingsService(this)
        initializeLocale()
    }

    private fun initializeLocale() {
        val language = settingsService.language
        if (language.isNotEmpty()) {
            val configuration = resources.configuration
            configuration.locale = (Locale(language))
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }
}