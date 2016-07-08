package com.irateam.vkplayer.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.adapters.AudioRecyclerViewAdapter;
import com.irateam.vkplayer.api.Query;
import com.irateam.vkplayer.models.Audio;

import java.util.List;

public class AudioListFragment extends Fragment {

    private final AudioRecyclerViewAdapter adapter = new AudioRecyclerViewAdapter();

    private Query<List<Audio>> query;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;

    public static AudioListFragment newInstance(Query<List<Audio>> query) {
        AudioListFragment fragment = new AudioListFragment();
        fragment.query = query;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

}
