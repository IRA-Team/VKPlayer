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
import com.irateam.vkplayer.services.PlayerService;
import com.irateam.vkplayer.viewholders.PlayerPanel;

import java.util.concurrent.TimeUnit;

public class AudioActivity extends AppCompatActivity implements ServiceConnection {

    private Toolbar toolbar;
    private ImageView imageView;

    private PlayerPanel playerPanel;
    private PlayerService playerService;

    Thread updateTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        toolbar = (Toolbar) findViewById(R.id.toolbar_transparent);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        imageView = (ImageView) findViewById(R.id.imageView);
        //imageView.setImageDrawable(getResources().getDrawable(R.drawable.audio_title_image));
        playerPanel = new PlayerPanel(this, findViewById(R.id.activity_player_panel));

        playerPanel.audioActivity = true;

        updateTime = new Thread() {

            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playerPanel.currentTime.setText(String.format("%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(playerPanel.progress.getProgress()),
                                    TimeUnit.MILLISECONDS.toSeconds(playerPanel.progress.getProgress()) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(playerPanel.progress.getProgress()))
                            ));
                            int timeRemaining = playerPanel.progress.getMax() - playerPanel.progress.getProgress();
                            playerPanel.timeToFinish.setText("-" + String.format("%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(timeRemaining),
                                    TimeUnit.MILLISECONDS.toSeconds(timeRemaining) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeRemaining))
                            ));
                        }
                    });
                }
            }
        };

        updateTime.start();
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
        //getMenuInflater().inflate(R.menu.menu_audio, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerPanel.audioActivity = false;
        updateTime.destroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i("Service", "Connected");
        playerService = ((PlayerService.PlayerBinder) service).getPlayerService();
        playerPanel.setPlayerService(playerService);
        playerService.addPlayerEventListener(playerPanel);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i("Service", "Disconnected");
    }
}
