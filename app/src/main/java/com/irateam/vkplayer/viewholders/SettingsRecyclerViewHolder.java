package com.irateam.vkplayer.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.irateam.vkplayer.R;

public class SettingsRecyclerViewHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public Switch aSwitch;

    public SettingsRecyclerViewHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.settings_recycler_view_name);
        aSwitch = (Switch) itemView.findViewById(R.id.settings_recycler_view_switch);
    }
}
