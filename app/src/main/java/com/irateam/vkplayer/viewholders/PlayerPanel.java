package com.irateam.vkplayer.viewholders;

import android.view.View;
import android.widget.TextView;

import com.irateam.vkplayer.R;
import com.vk.sdk.api.model.VKApiAudio;

public class PlayerPanel {

    public View rootView;
    public TextView songName;
    public TextView author;

    public PlayerPanel(View view) {
        rootView = view;
        songName = (TextView) view.findViewById(R.id.player_panel_song_name);
        author = (TextView) view.findViewById(R.id.player_panel_author);
    }

    public void setAudio(VKApiAudio audio) {
        songName.setText(audio.title);
        author.setText(audio.artist);
    }
}
