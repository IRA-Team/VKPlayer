package com.irateam.vkplayer.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.player.Player;
import com.vk.sdk.api.model.VKApiAudio;

public class PlayerPanel {

    public View rootView;
    public TextView songName;
    public TextView author;

    public ImageView repeat;
    public ImageView previous;
    public ImageView playPause;
    public ImageView next;
    public ImageView random;

    public PlayerPanel(View view) {
        rootView = view;
        songName = (TextView) view.findViewById(R.id.player_panel_song_name);
        author = (TextView) view.findViewById(R.id.player_panel_author);

        repeat = (ImageView) view.findViewById(R.id.player_panel_repeat);
        previous = (ImageView) view.findViewById(R.id.player_panel_previous);
        playPause = (ImageView) view.findViewById(R.id.player_panel_play_pause);
        next = (ImageView) view.findViewById(R.id.player_panel_next);
        random = (ImageView) view.findViewById(R.id.player_panel_random);
    }

    public void setAudio(VKApiAudio audio) {
        songName.setText(audio.title);
        author.setText(audio.artist);
    }

    public void setPlayer(final Player player) {
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.previous();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.next();
            }
        });
    }
}
