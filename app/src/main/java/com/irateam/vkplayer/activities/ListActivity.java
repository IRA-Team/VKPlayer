package com.irateam.vkplayer.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.adapter.AudioAdapter;
import com.irateam.vkplayer.player.Player;
import com.irateam.vkplayer.player.ServerProxy;
import com.irateam.vkplayer.services.AudioService;
import com.irateam.vkplayer.viewholders.PlayerPanel;
import com.mobeta.android.dslv.DragSortListView;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VKApiAudio;

import java.util.List;

public class ListActivity extends AppCompatActivity implements
        AudioService.Listener,
        NavigationView.OnNavigationItemSelectedListener,
        AdapterView.OnItemClickListener, Player.Listener,
        DragSortListView.DropListener,
        SwipeRefreshLayout.OnRefreshListener {

    private Player player = Player.getInstance();
    private AudioAdapter audioAdapter = new AudioAdapter(this);
    private AudioService audioService = new AudioService();
    private ServerProxy serverProxy = new ServerProxy();

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CoordinatorLayout coordinatorLayout;
    private PlayerPanel playerPanel;
    private SwipeRefreshLayout refreshLayout;
    private DragSortListView listView;

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

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        playerPanel = new PlayerPanel(findViewById(R.id.player_panel));
        playerPanel.rootView.setVisibility(View.GONE);
        playerPanel.setPlayer(player);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(
                R.color.accent,
                R.color.primary
        );
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setRefreshing(true);

        listView = (DragSortListView) findViewById(R.id.list);
        listView.setAdapter(audioAdapter);
        listView.setOnItemClickListener(this);
        listView.setDropListener(this);

        //Services
        audioService.addListener(this);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));

        player.addListener(this);
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
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onComplete(List<VKApiAudio> list) {
        refreshLayout.setRefreshing(false);
        audioAdapter.setList(list);
        audioAdapter.notifyDataSetChanged();
        System.out.println("Complete" + list.size());
    }

    @Override
    public void onError(VKError error) {
        refreshLayout.setRefreshing(false);
        Snackbar.make(coordinatorLayout, error.errorMessage, Snackbar.LENGTH_LONG)
                .setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        audioService.getMyAudio();
                    }
                })
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        drawerLayout.closeDrawers();
        getSupportActionBar().setTitle(menuItem.getTitle());
        refreshLayout.setRefreshing(true);
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
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (audioAdapter.getList() != player.getList()) {
            player.setList(audioAdapter.getList());
        }
        player.play(position);
    }

    @Override
    public void onAudioChanged(VKApiAudio audio) {
        if (playerPanel.rootView.getVisibility() == View.GONE) {
            playerPanel.rootView.setVisibility(View.VISIBLE);
        }
        playerPanel.setAudio(audio);
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
}
