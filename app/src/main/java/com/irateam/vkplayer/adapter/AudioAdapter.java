package com.irateam.vkplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.irateam.vkplayer.R;
import com.vk.sdk.api.model.VKApiAudio;

import java.util.ArrayList;
import java.util.List;

public class AudioAdapter extends BaseAdapter {

    private Context context;
    private List<VKApiAudio> list;

    private ColorGenerator colorGenerator;

    public AudioAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();

        colorGenerator = ColorGenerator.MATERIAL;
    }

    public List<VKApiAudio> getList() {
        return list;
    }

    public void setList(List<VKApiAudio> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).id;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.player_list_element, parent, false);

        VKApiAudio audio = list.get(position);
        audio.artist = audio.artist.trim();

        TextView songName = (TextView) view.findViewById(R.id.player_list_element_song_name);
        TextView author = (TextView) view.findViewById(R.id.player_list_element_author);
        ImageView cover = (ImageView) view.findViewById(R.id.player_list_element_cover);

        songName.setText(audio.title);
        author.setText(audio.artist);
        cover.setImageDrawable(TextDrawable.builder()
                .buildRound(String.valueOf(audio.artist.charAt(0)), colorGenerator.getColor(audio.artist)));
        return view;
    }
}
