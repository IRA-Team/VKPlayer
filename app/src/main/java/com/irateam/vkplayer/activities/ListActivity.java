package com.irateam.vkplayer.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.player.Player;
import com.irateam.vkplayer.player.ServerProxy;
import com.irateam.vkplayer.services.AudioService;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.model.VKApiAudio;

import java.io.IOException;
import java.util.List;

public class ListActivity extends AppCompatActivity implements AudioService.Listener {

    Player player = Player.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        AudioService audioService = new AudioService();
        audioService.addListener(this);
        audioService.getMyAudio();

        final ServerProxy serverProxy = new ServerProxy();
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKSdk.logout();
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    serverProxy.start();
                    Log.i("Server", "Server start");
                    player.play(0);
                    Log.i("Player", "Player play start");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onComplete(List<VKApiAudio> list) {
        player.setList(list);
        System.out.println("Complete" + list.size());
    }
}
