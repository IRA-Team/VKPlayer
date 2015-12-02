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

package com.irateam.vkplayer.activities.settings;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.models.Settings;
import com.irateam.vkplayer.services.AudioService;
import com.irateam.vkplayer.services.DownloadService;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
        toolbar.setTitle(getResources().getString(R.string.title_activity_settings));
        root.addView(toolbar, 0);
        toolbar.setNavigationOnClickListener(v -> finish());
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }


    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();
        preference.setSummary(stringValue);
        return true;
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SyncPreferenceFragment.class.getName().equals(fragmentName)
                || CachePreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Settings.SYNC_TIME)) {
            Settings.setSyncAlarm(this);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SyncPreferenceFragment extends PreferenceFragment {

        private Context context;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_sync);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("sync_time"));
            bindPreferenceSummaryToValue(findPreference("sync_count"));
            bindPreferenceSummaryToValue(findPreference("sync_button"));

            findPreference("sync_button").setOnPreferenceClickListener(preference -> {
                context = getActivity();
                Intent intent = new Intent(context, DownloadService.class);
                intent.putExtra(DownloadService.USER_SYNC, true);
                intent.setAction(DownloadService.START_SYNC);
                context.startService(intent);
                return false;
            });

            findPreference("sync_enabled").setOnPreferenceChangeListener(((preference, newValue) -> {
                boolean syncEnabled = (Boolean) newValue;
                if (syncEnabled) {
                    Settings.setSyncAlarm(getActivity());
                } else {
                    Settings.cancelSyncAlarm(getActivity());
                }
                return true;
            }));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CachePreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_cache);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("cache_clear"));

            findPreference("cache_clear").setOnPreferenceClickListener(preference -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.clear_cache_dialog_title))
                        .setMessage(getString(R.string.clear_cache_dialog_text))
                        .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                            new AudioService(getActivity()).removeAllCachedAudio();
                            Toast.makeText(getActivity(), getString(R.string.cache_clear_complete), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(getString(R.string.no), (dialog, id) -> {

                        });
                builder.create().show();
                return false;
            });


        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

}
