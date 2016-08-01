package com.irateam.vkplayer.api.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.preference.PreferenceManager
import com.irateam.vkplayer.player.Player
import com.irateam.vkplayer.service.DownloadService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsService {

    private val context: Context
    private val preferences: SharedPreferences

    constructor(context: Context) {
        this.context = context
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun saveRepeatState(state: Player.RepeatState) = preferences.edit()
            .putString(REPEAT_STATE, state.name)
            .apply()

    fun loadRepeatState(): Player.RepeatState {
        val rawRepeatState = preferences.getString(REPEAT_STATE, Player.RepeatState.NO_REPEAT.name)
        return Player.RepeatState.valueOf(rawRepeatState)
    }

    fun saveRandomState(state: Boolean) = preferences.edit()
            .putBoolean(RANDOM_STATE, state)
            .apply()

    fun loadRandomState(): Boolean {
        return preferences.getBoolean(RANDOM_STATE, false)
    }

    fun saveSyncEnabled(enabled: Boolean) = preferences.edit()
            .putBoolean(SYNC_ENABLED, enabled)
            .apply()

    fun loadSyncEnabled(): Boolean {
        return preferences.getBoolean(SYNC_ENABLED, false)
    }

    fun saveWifiSync(isWifi: Boolean) = preferences.edit()
            .putBoolean(SYNC_WIFI, isWifi)
            .apply()

    fun loadWifiSync(): Boolean {
        return preferences.getBoolean(SYNC_WIFI, false)
    }

    //TODO: Probably should be refactored
    fun saveSyncTime(hour: Int, minutes: Int) = preferences.edit()
            .putString(SYNC_TIME, "%02d".format(hour) + ":" + "%02d".format(minutes))
            .apply()

    fun loadSyncTime(): Calendar {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        val date = simpleDateFormat.parse(preferences.getString(SYNC_TIME, "18:30"))

        calendar.set(Calendar.HOUR_OF_DAY, date!!.hours)
        calendar.set(Calendar.MINUTE, date.minutes)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1)
        }
        return calendar
    }

    fun saveSyncCount(count: Int) = preferences.edit()
            .putInt(SYNC_COUNT, count)
            .apply()

    fun loadSyncCount(): Int {
        val count = preferences.getInt(SYNC_COUNT, -1)
        return if (count > 0) count else DEFAULT_SYNC_COUNT
    }

    fun getAudioCacheDir(): File {
        val state = Environment.getExternalStorageState()
        return if (Environment.MEDIA_MOUNTED == state) {
            context.getExternalFilesDir(null)
        } else {
            context.filesDir
        }
    }

    fun setSyncAlarm() {
        val intent = DownloadService.startSyncIntent(context, false)
        val pendingIntent = PendingIntent.getService(context,
                SYNC_ALARM_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                loadSyncTime().timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent)
    }

    fun cancelSyncAlarm() {
        val intent = DownloadService.startSyncIntent(context, false)
        val pendingIntent = PendingIntent.getService(context,
                SYNC_ALARM_ID,
                intent,
                0)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    companion object {

        private val SYNC_ALARM_ID = 1
        private val DEFAULT_SYNC_COUNT = 10

        @JvmField val REPEAT_STATE = "repeat_state"
        @JvmField val RANDOM_STATE = "random_state"
        @JvmField val SYNC_ENABLED = "sync_enabled"
        @JvmField val SYNC_TIME = "sync_time"
        @JvmField val SYNC_COUNT = "sync_count"
        @JvmField val SYNC_WIFI = "sync_wifi"
    }
}
