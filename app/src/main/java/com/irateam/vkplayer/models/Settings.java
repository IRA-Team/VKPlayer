package com.irateam.vkplayer.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.irateam.vkplayer.player.Player;

public class Settings {

    public static final String REPEAT_STATE = "repeat_state";
    public static final String RANDOM_STATE = "random_state";

    public static final String SYNC_ENABLED = "sync_enabled";

    private static Settings instance;

    public static synchronized Settings getInstance(Context context) {
        if (instance == null) {
            instance = new Settings(context);
        }
        return instance;
    }

    private SharedPreferences preferences;

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

    public boolean getSyncEnabled() {
        return preferences.getBoolean(SYNC_ENABLED, false);
    }
}