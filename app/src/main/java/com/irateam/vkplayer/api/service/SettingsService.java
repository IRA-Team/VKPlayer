package com.irateam.vkplayer.api.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.irateam.vkplayer.player.Player;
import com.irateam.vkplayer.service.DownloadService;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class SettingsService {

    private static final int SYNC_ALARM_ID = 1;
    private static final int DEFAULT_SYNC_COUNT = 10;

    public static final String REPEAT_STATE = "repeat_state";
    public static final String RANDOM_STATE = "random_state";
    public static final String SYNC_ENABLED = "sync_enabled";
    public static final String SYNC_TIME = "sync_time";
    public static final String SYNC_COUNT = "sync_count";
    public static final String SYNC_WIFI = "sync_wifi";

    private final Context context;
    private final SharedPreferences preferences;

    private static SettingsService instance;

    public static synchronized SettingsService getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsService(context);
        }
        return instance;
    }

    public SettingsService(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public void saveRepeatState(Player.RepeatState state) {
        preferences.edit()
                .putString(REPEAT_STATE, state.name())
                .apply();
    }

    public Player.RepeatState loadRepeatState() {
        return Player.RepeatState.valueOf(preferences.getString(REPEAT_STATE, Player.RepeatState.NO_REPEAT.name()));
    }

    public void saveRandomState(boolean state) {
        preferences.edit()
                .putBoolean(RANDOM_STATE, state)
                .apply();
    }

    public boolean loadRandomState() {
        return preferences.getBoolean(RANDOM_STATE, false);
    }

    public void saveSyncEnabled(boolean enabled) {
        preferences.edit()
                .putBoolean(SYNC_ENABLED, enabled)
                .apply();
    }

    public boolean loadSyncEnabled() {
        return preferences.getBoolean(SYNC_ENABLED, false);
    }

    public void saveWifiSync(boolean isWifi) {
        preferences.edit()
                .putBoolean(SYNC_WIFI, isWifi)
                .apply();
    }

    public boolean loadWifiSync() {
        return preferences.getBoolean(SYNC_WIFI, false);
    }

    public void saveSyncTime(int hour, int minutes) {
        preferences.edit()
                .putString(SYNC_TIME, String.format("%02d", hour) + ":" + String.format("%02d", minutes))
                .apply();
    }

    public Calendar loadSyncTime() {
        Calendar calendar = Calendar.getInstance();
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        try {
            date = simpleDateFormat.parse(preferences.getString(SYNC_TIME, "18:30"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        calendar.set(Calendar.HOUR_OF_DAY, date.getHours());
        calendar.set(Calendar.MINUTE, date.getMinutes());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
        }
        return calendar;
    }

    public void saveSyncCount(int count) {
        preferences.edit()
                .putString(SYNC_COUNT, String.valueOf(count))
                .apply();
    }

    public int loadSyncCount() {
        if (preferences.getString(SYNC_COUNT, "").isEmpty())
            return DEFAULT_SYNC_COUNT;
        else
            return Integer.valueOf(preferences.getString(SYNC_COUNT, String.valueOf(DEFAULT_SYNC_COUNT)));
    }

    public File getAudioCacheDir() {
        String state = Environment.getExternalStorageState();
        File cacheDir;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            cacheDir = context.getExternalFilesDir(null);
        } else {
            cacheDir = context.getFilesDir();
        }
        return cacheDir;
    }

    public void setSyncAlarm() {
        Intent intent = DownloadService.startSyncIntent(context, false);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                SYNC_ALARM_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                loadSyncTime().getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }

    public void cancelSyncAlarm() {
        Intent intent = DownloadService.startSyncIntent(context, false);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                SYNC_ALARM_ID,
                intent,
                0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
