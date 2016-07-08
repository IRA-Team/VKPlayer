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

package com.irateam.vkplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;

import com.irateam.vkplayer.api.SimpleCallback;
import com.irateam.vkplayer.api.service.AudioInfoService;
import com.irateam.vkplayer.api.service.SettingsService;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.notifications.PlayerNotificationFactory;
import com.irateam.vkplayer.player.Player;
import com.irateam.vkplayer.player.PlayerEvent;
import com.irateam.vkplayer.player.PlayerPauseEvent;
import com.irateam.vkplayer.player.PlayerPlayEvent;
import com.irateam.vkplayer.player.PlayerResumeEvent;
import com.irateam.vkplayer.player.PlayerStopEvent;
import com.irateam.vkplayer.receivers.DownloadFinishedReceiver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

public class PlayerService extends Service implements AudioManager.OnAudioFocusChangeListener {

    public static final int PLAYER_NOTIFICATION_ID = 1;

    public static final String PREVIOUS = "playerService.PREVIOUS";
    public static final String PAUSE = "playerService.PAUSE";
    public static final String RESUME = "playerService.RESUME";
    public static final String NEXT = "playerService.NEXT";
    public static final String STOP = "playerService.STOP";

    private final Player player = Player.getInstance();
    private final EventBus eventBus = EventBus.getDefault();
    private final AudioInfoService audioInfoService = new AudioInfoService(this);
    private SettingsService settingsService;
    private PlayerNotificationFactory notificationFactory;
    private final Binder binder = new PlayerBinder();

    private BroadcastReceiver headsetReceiver;
    private AudioManager audioManager;
    private NotificationManager notificationManager;

    private boolean removeNotification = false;
    private boolean wasPlaying = false;
    private boolean hasFocus = false;
    private DownloadFinishedReceiver downloadFinishedReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        this.settingsService = SettingsService.getInstance(this);
        this.notificationFactory = new PlayerNotificationFactory(this);

        player.setRepeatState(settingsService.getPlayerRepeat());
        player.setRandomState(settingsService.getRandomState());

        eventBus.register(this);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        headsetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                    if (intent.getIntExtra("state", -1) == 0) {
                        if (player.isPlaying()) {
                            pause(false);
                        }
                    }
                }
            }
        };

        downloadFinishedReceiver = new DownloadFinishedReceiver() {
            @Override
            public void onDownloadFinished(Audio downloaded) {
                for (Audio audio : getPlaylist()) {
                    if (audio.equalsId(downloaded)) {
                        audio.setCacheFile(downloaded.getCacheFile());
                    }
                }
            }
        };

        registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        registerReceiver(downloadFinishedReceiver, new IntentFilter(DownloadService.DOWNLOAD_FINISHED));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case PREVIOUS:
                    player.previous();
                    break;

                case PAUSE:
                    player.pause();
                    break;

                case RESUME:
                    player.resume();
                    break;

                case NEXT:
                    player.next();
                    break;

                case STOP:
                    player.stop();
                    break;
            }
        }
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        eventBus.unregister(this);
        unregisterReceiver(headsetReceiver);
        unregisterReceiver(downloadFinishedReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //Player methods
    public void setPlaylist(List<Audio> list) {
        player.setQueue(list);
    }

    public List<Audio> getPlaylist() {
        return player.getQueue();
    }

    public void play(int index) {
        if (!hasFocus()) {
            requestFocus();
        }
        player.play(index);
    }

    public void resume() {
        if (!hasFocus()) {
            requestFocus();
        }
        player.resume();
    }

    public void pause() {
        pause(true, true);
    }

    public void pause(boolean removeNotification) {
        pause(removeNotification, true);
    }

    public void pause(boolean removeNotification, boolean abandonFocus) {
        if (abandonFocus && hasFocus()) {
            abandonFocus();
        }
        this.removeNotification = removeNotification;
        player.pause();
    }

    public void stop() {
        if (hasFocus()) {
            abandonFocus();
        }
        player.stop();
    }

/*
TODO: settings
    public void setRepeatState(Player.RepeatState state) {
        settings.setPlayerRepeat(state);
    }

    public Player.RepeatState switchRepeatState() {
        settings.setPlayerRepeat(state);
    }


    public boolean switchRandomState() {
        settings.setRandomState(state);
    }
*/


    //Player callbacks

    @Subscribe
    public void onPlayEvent(PlayerPlayEvent e) {
        final int index = e.getIndex();
        final Audio audio = e.getAudio();

        startForeground(PLAYER_NOTIFICATION_ID, notificationFactory.get(e));
        audioInfoService.get(audio).execute(SimpleCallback.of(info -> {
            audio.setAudioInfo(info);
            updateNotification(index, audio);
        }));
    }

    @Subscribe
    public void onPauseEvent(PlayerPauseEvent e) {
        if (removeNotification) {
            stopForeground(true);
        } else {
            updateNotification(e);
        }
    }

    @Subscribe
    public void onResumeEvent(PlayerResumeEvent e) {
        startForeground(PLAYER_NOTIFICATION_ID, notificationFactory.get(e));
    }

    @Subscribe
    public void onStopEvent(PlayerStopEvent e) {
        stopForeground(true);
    }

    public void updateNotification(int index, Audio audio) {
        Notification notification = notificationFactory.get(index, audio);
        notificationManager.notify(PLAYER_NOTIFICATION_ID, notification);
    }


    public void updateNotification(PlayerEvent e) {
        Notification notification = notificationFactory.get(e);
        notificationManager.notify(PLAYER_NOTIFICATION_ID, notification);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                wasPlaying = player.isPlaying();
                pause(false, false);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                wasPlaying = player.isPlaying();
                pause(false, false);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (wasPlaying) {
                    resume();
                }
                break;
        }
    }

    private boolean hasFocus() {
        return hasFocus;
    }

    private void requestFocus() {
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        hasFocus = true;
    }

    private void abandonFocus() {
        audioManager.abandonAudioFocus(this);
        hasFocus = false;
    }

    public class PlayerBinder extends Binder {
        public PlayerService getPlayerService() {
            return PlayerService.this;
        }
    }
}