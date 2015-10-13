package com.irateam.vkplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.viewholders.SettingsRecyclerViewHolder;

import java.util.ArrayList;

public class SettingsRecyclerViewAdapter extends RecyclerView.Adapter<SettingsRecyclerViewHolder> {

    private ArrayList<String> names;

    public SettingsRecyclerViewAdapter(ArrayList<String> names) {
        this.names = names;
    }

    @Override
    public SettingsRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.settings_recycler_view_item, viewGroup, false);
        return new SettingsRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SettingsRecyclerViewHolder holder, int position) {
        holder.name.setText(names.get(position));
    }

    @Override
    public int getItemCount() {
        return names.size();
    }
}
