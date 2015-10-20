package com.irateam.vkplayer.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.controllers.ActivityPlayerController;
import com.irateam.vkplayer.controllers.PlayerController;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.services.AudioService;
import com.irateam.vkplayer.services.DownloadService;
import com.irateam.vkplayer.services.PlayerService;

import java.util.ArrayList;
import java.util.List;

public class AudioActivity extends AppCompatActivity implements ServiceConnection {

    private Toolbar toolbar;
    private ImageView imageView;

    private PlayerController playerController;
    private PlayerService playerService;
    private AudioService audioService = new AudioService(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);

        toolbar = (Toolbar) findViewById(R.id.toolbar_transparent);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.player_cover));
        playerController = new ActivityPlayerController(this, findViewById(R.id.activity_player_panel));
        playerController.setFabOnClickListener(v -> finish());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, PlayerService.class));
        bindService(new Intent(this, PlayerService.class), this, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_audio, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return (super.onOptionsItemSelected(item));
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i("Service", "Connected");
        playerService = ((PlayerService.PlayerBinder) service).getPlayerService();
        playerController.setPlayerService(playerService);
        playerService.addPlayerEventListener(playerController);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i("Service", "Disconnected");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_out_up_close, R.anim.slide_in_up_close);
    }
}
