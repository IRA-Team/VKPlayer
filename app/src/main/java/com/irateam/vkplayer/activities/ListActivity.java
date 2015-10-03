package com.irateam.vkplayer.activities;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.adapter.AudioAdapter;
import com.irateam.vkplayer.player.Player;
import com.irateam.vkplayer.player.ServerProxy;
import com.irateam.vkplayer.services.AudioService;
import com.mobeta.android.dslv.DragSortListView;
import com.vk.sdk.api.model.VKApiAudio;

import java.util.List;

public class ListActivity extends AppCompatActivity implements AudioService.Listener {

    private Player player = Player.getInstance();
    private AudioAdapter audioAdapter = new AudioAdapter(this);
    private AudioService audioService = new AudioService();
    private ServerProxy serverProxy = new ServerProxy();

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setUpToolbar();
        setUpNavDrawer();


        audioService.addListener(this);
        audioService.getMyAudio();

        DragSortListView listView = (DragSortListView) findViewById(R.id.view);
        listView.setAdapter(audioAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setUpNavDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public void onComplete(List<VKApiAudio> list) {
        player.setList(list);
        audioAdapter.setList(list);
        audioAdapter.notifyDataSetChanged();
        System.out.println("Complete" + list.size());
    }
}
