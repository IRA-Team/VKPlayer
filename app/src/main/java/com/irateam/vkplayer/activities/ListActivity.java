package com.irateam.vkplayer.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.adapter.AudioAdapter;
import com.irateam.vkplayer.player.Player;
import com.irateam.vkplayer.player.ServerProxy;
import com.irateam.vkplayer.services.AudioService;
import com.irateam.vkplayer.services.PlayerService;
import com.irateam.vkplayer.ui.RoundImageView;
import com.irateam.vkplayer.viewholders.PlayerPanel;
import com.mobeta.android.dslv.DragSortListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VKApiUser;

import org.json.JSONException;

import java.util.List;

public class ListActivity extends AppCompatActivity implements
        AudioService.Listener,
        NavigationView.OnNavigationItemSelectedListener,
        AdapterView.OnItemClickListener,
        DragSortListView.DropListener,
        SwipeRefreshLayout.OnRefreshListener, ServiceConnection {

    private AudioAdapter audioAdapter = new AudioAdapter(this);
    private AudioService audioService = new AudioService(this);

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RoundImageView roundImageView;
    private TextView userFullName;
    private TextView userVkId;

    private CoordinatorLayout coordinatorLayout;
    private SwipeRefreshLayout refreshLayout;
    private DragSortListView listView;

    private PlayerPanel playerPanel;
    private PlayerService playerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //Views
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        roundImageView = (RoundImageView) findViewById(R.id.navigation_drawer_header_avatar);
        userFullName = (TextView) findViewById(R.id.navigation_drawer_header_full_name);
        userVkId = (TextView) findViewById(R.id.navigation_drawer_header_id);

        VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "photo_100")).executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    VKApiUser user = new VKApiUser().parse(response.json.getJSONArray("response").getJSONObject(0));
                    ImageLoader.getInstance().displayImage(user.photo_100, roundImageView);
                    userFullName.setText(user.first_name + " " + user.last_name);
                    userVkId.setText(String.valueOf(user.id));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        playerPanel = new PlayerPanel(this, findViewById(R.id.player_panel));
        playerPanel.rootView.setVisibility(View.GONE);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(
                R.color.accent,
                R.color.primary
        );
        refreshLayout.setOnRefreshListener(this);

        listView = (DragSortListView) findViewById(R.id.list);
        listView.setAdapter(audioAdapter);
        listView.setOnItemClickListener(this);
        listView.setDropListener(this);

        audioService.addListener(this);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));

        startService(new Intent(this, PlayerService.class));
        bindService(new Intent(this, PlayerService.class), this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerService.removePlayerEventListener(playerPanel);
        unbindService(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:
                boolean flag = !listView.isDragEnabled();
                listView.setDragEnabled(flag);
                audioAdapter.setSortMode(flag);
                refreshLayout.setEnabled(!flag);
                return true;
            case R.id.audio_activity:
                startActivity(new Intent(this, AudioActivity.class));
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onComplete(List<VKApiAudio> list) {
        refreshLayout.setRefreshing(false);
        audioAdapter.setList(list);
        audioAdapter.notifyDataSetChanged();
    }

    @Override
    public void onError(String errorMessage) {
        refreshLayout.setRefreshing(false);
        Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.title_snackbar_action), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        audioService.repeatLastRequest();
                    }
                })
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        drawerLayout.closeDrawers();

        if (menuItem.getGroupId() == R.id.audio_group) {
            getSupportActionBar().setTitle(menuItem.getTitle());
            refreshLayout.setRefreshing(true);
        }

        switch (menuItem.getItemId()) {
            case R.id.my_audio:
                audioService.getMyAudio();
                return true;
            case R.id.recommended_audio:
                audioService.getRecommendationAudio();
                return true;
            case R.id.popular_audio:
                audioService.getPopularAudio();
                return true;

            case R.id.exit:
                VkLogout();
                return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (playerService != null) {
            playerService.setPlaylist(audioAdapter.getList());
            playerService.play(position);
        }
    }

    @Override
    public void drop(int from, int to) {
        List<VKApiAudio> list = audioAdapter.getList();
        VKApiAudio audio = list.get(from);
        list.remove(from);
        list.add(to, audio);
        audioAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        audioService.repeatLastRequest();
    }

    @Override
    public void onBackPressed() {
        if (audioAdapter.isSortMode()) {
            audioAdapter.setSortMode(false);
            listView.setDragEnabled(false);
            refreshLayout.setEnabled(true);
            return;
        }
        super.onBackPressed();
    }

    private void VkLogout() {
        VKSdk.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
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
