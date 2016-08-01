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

package com.irateam.vkplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.api.SimpleCallback;
import com.irateam.vkplayer.api.service.AudioService;
import com.irateam.vkplayer.controller.ActivityPlayerController;
import com.irateam.vkplayer.controller.PlayerController;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.player.Player;
import com.irateam.vkplayer.service.DownloadService;
import com.irateam.vkplayer.service.PlayerService;
import com.irateam.vkplayer.util.EventBus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AudioActivity extends AppCompatActivity {

    private final Player player = Player.getInstance();
    private final EventBus eventBus = EventBus.INSTANCE;

    private Toolbar toolbar;

    private PlayerController playerController;
    private AudioService audioService = new AudioService(this);
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);

        toolbar = (Toolbar) findViewById(R.id.toolbar_transparent);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        playerController = new ActivityPlayerController(this, findViewById(R.id.activity_player_panel));
        playerController.initialize();
        eventBus.register(playerController);
        playerController.setFabOnClickListener(v -> finish());

        startService(new Intent(this, PlayerService.class));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_audio, menu);
        Audio audio = player.getPlayingAudio();
        if (audio != null) {
            setCacheAction(audio.isCached());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_cache:
                List<Audio> list = Arrays.asList(player.getPlayingAudio());
                DownloadService.download(this, list);
                break;

            case R.id.action_remove_from_cache:
                List<Audio> removeList = Collections.singletonList(player.getPlayingAudio());
                audioService.removeFromCache(removeList).execute(SimpleCallback.success(audios -> {
                    setCacheAction(false);
                }));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_out_up_close, R.anim.slide_in_up_close);
    }

    public void setCacheAction(boolean isCached) {
        if (menu != null) {
            if (isCached) {
                menu.findItem(R.id.action_remove_from_cache).setVisible(true);
                menu.findItem(R.id.action_cache).setVisible(false);
            } else {
                menu.findItem(R.id.action_remove_from_cache).setVisible(false);
                menu.findItem(R.id.action_cache).setVisible(true);
            }
        }
    }
}
