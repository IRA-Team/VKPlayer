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

package com.irateam.vkplayer.models;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.irateam.vkplayer.player.Player;
import com.irateam.vkplayer.services.DownloadService;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Settings {

    public static final String REPEAT_STATE = "repeat_state";
    public static final String RANDOM_STATE = "random_state";

    public static final String SYNC_ENABLED = "sync_enabled";
    public static final String SYNC_TIME = "sync_time";
    public static final String SYNC_COUNT = "sync_count";
    public static final String SYNC_WIFI = "sync_wifi";

    private static final int SYNC_ALARM_ID = 1;
    private static final int DEFAULT_SYNC_COUNT = 10;

    private static Settings instance;

    private SharedPreferences preferences;

    public static synchronized Settings getInstance(Context context) {
        if (instance == null) {
            instance = new Settings(context);
        }
        return instance;
    }

    private Settings(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setPlayerRepeat(Player.RepeatState state) {
        preferences.edit()
                .putString(REPEAT_STATE, state.name())
                .apply();
    }

    public Player.RepeatState getPlayerRepeat() {
        return Player.RepeatState.valueOf(preferences.getString(REPEAT_STATE, Player.RepeatState.NO_REPEAT.name()));
    }

    public void setRandomState(boolean state) {
        preferences.edit()
                .putBoolean(RANDOM_STATE, state)
                .apply();
    }

    public boolean getRandomState() {
        return preferences.getBoolean(RANDOM_STATE, false);
    }

    public void setSyncEnabled(boolean enabled) {
        preferences.edit()
                .putBoolean(SYNC_ENABLED, enabled)
                .apply();
    }

    public boolean isSyncEnabled() {
        return preferences.getBoolean(SYNC_ENABLED, false);
    }

    public void setSyncWifi(boolean isWifi) {
        preferences.edit()
                .putBoolean(SYNC_WIFI, isWifi)
                .apply();
    }

    public boolean isWifiSync() {
        return preferences.getBoolean(SYNC_WIFI, false);
    }

    public void setSyncTime(int hour, int minutes) {
        preferences.edit()
                .putString(SYNC_TIME, String.format("%02d", hour) + ":" + String.format("%02d", minutes))
                .apply();
    }

    public Calendar getSyncTime() {
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

    public void setSyncCount(int count) {
        preferences.edit()
                .putString(SYNC_COUNT, String.valueOf(count))
                .apply();
    }

    public int getSyncCount() {
        if (preferences.getString(SYNC_COUNT, "").isEmpty())
            return DEFAULT_SYNC_COUNT;
        else
            return Integer.valueOf(preferences.getString(SYNC_COUNT, String.valueOf(DEFAULT_SYNC_COUNT)));
    }

    public File getAudioCacheDir(Context context) {
        String state = Environment.getExternalStorageState();
        File cacheDir;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            cacheDir = context.getExternalFilesDir(null);
        } else {
            cacheDir = context.getFilesDir();
        }
        return cacheDir;
    }

    public static void setSyncAlarm(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.START_SYNC);
        intent.putExtra(DownloadService.USER_SYNC, false);
        PendingIntent pendingIntent = PendingIntent.getService(context, SYNC_ALARM_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                Settings.getInstance(context).getSyncTime().getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }

    public static void cancelSyncAlarm(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.START_SYNC);
        PendingIntent pendingIntent = PendingIntent.getService(context, SYNC_ALARM_ID, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}