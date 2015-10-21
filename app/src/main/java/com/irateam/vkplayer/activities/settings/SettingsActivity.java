package com.irateam.vkplayer.activities.settings;


import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.models.Settings;
import com.irateam.vkplayer.receivers.NotificationReceiver;
import com.irateam.vkplayer.services.DownloadService;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
        toolbar.setTitle(getResources().getString(R.string.title_activity_settings));
        root.addView(toolbar, 0);
        toolbar.setNavigationOnClickListener(v -> finish());
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
                || SyncPreferenceFragment.class.getName().equals(fragmentName);
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
                intent.setAction(DownloadService.START_SYNC);
                context.startService(intent);
                return false;
            });

            findPreference("sync_enabled").setOnPreferenceChangeListener(((preference, newValue) -> {
                boolean syncEnabled = (Boolean) newValue;
                if (syncEnabled) {
                    setSyncAlarm(getActivity());
                } else {
                    cancelSyncAlarm(getActivity());
                }
                return true;
            }));
        }

        public static void setSyncAlarm(Context context) {
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("player.STOP_SERVICE");
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    Settings.getInstance(context).getSyncTime().getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent);
        }

        public static void cancelSyncAlarm(Context context) {
            Intent intent = new Intent(context, DownloadService.class);
            intent.setAction(DownloadService.START_SYNC);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
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
