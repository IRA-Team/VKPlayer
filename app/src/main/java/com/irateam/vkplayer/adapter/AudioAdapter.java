package com.irateam.vkplayer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.ui.AudioListElement;
import com.irateam.vkplayer.utils.AlbumCoverUtils;
import com.vk.sdk.api.model.VKApiAudio;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AudioAdapter extends BaseAdapter {

    private Context context;
    private List<VKApiAudio> list = new ArrayList<>();
    private List<Integer> checkedList = new ArrayList<>();

    private boolean sortMode = false;

    public AudioAdapter(Context context) {
        this.context = context;
    }

    public List<VKApiAudio> getList() {
        return list;
    }

    public void setList(List<VKApiAudio> list) {
        this.checkedList = new ArrayList<>();
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
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.player_list_element, parent, false);

        AudioListElement element = (AudioListElement) view;

        final VKApiAudio audio = list.get(position);
        audio.artist = audio.artist.trim();

        element.setTitle(audio.title);
        element.setArtist(audio.artist);
        element.setCover(AlbumCoverUtils.createFromAudio(audio));
        element.setCoverOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyCoverChecked(position);
            }
        });
        element.setDuration(audio.duration);

        if (checkedList.contains(position)) {
            element.setChecked(true);
        }
        return view;
    }

    public void toggleChecked(int position) {
        if (checkedList.contains(position)) {
            checkedList.remove(checkedList.indexOf(position));
        } else {
            checkedList.add(position);
        }
        System.out.println(checkedList.size());
        notifyDataSetChanged();
    }

    public List<Integer> getCheckedIndexList() {
        return checkedList;
    }

    public List<VKApiAudio> getCheckedItems() {
        List<VKApiAudio> list = new ArrayList<>();
        for (Integer i : checkedList) {
            list.add(this.list.get(i));
        }
        return list;
    }

    public int getCheckedCount() {
        return checkedList.size();
    }

    public void clearChecked() {
        checkedList = new ArrayList<>();
        notifyDataSetChanged();
    }


    private void setSortedCover(ImageView v) {
        Drawable[] layers = new Drawable[2];
        layers[0] = v.getDrawable();
        layers[1] = context.getResources().getDrawable(R.drawable.player_list_element_cover_overlay);
        v.setImageDrawable(new LayerDrawable(layers));
    }

    public boolean isSortMode() {
        return sortMode;
    }


    public void setSortMode(boolean sortMode) {
        if (this.sortMode != sortMode) {
            checkedList = new ArrayList<>();
            notifyDataSetChanged();
        }
        this.sortMode = sortMode;
    }

    public interface CoverCheckListener {
        void onCoverCheck(int position);
    }

    private WeakReference<CoverCheckListener> listener;

    public void setCoverCheckListener(CoverCheckListener listener) {
        this.listener = new WeakReference<CoverCheckListener>(listener);
    }

    public void notifyCoverChecked(int position) {
        if (listener != null && listener.get() != null) {
            listener.get().onCoverCheck(position);
        }
    }
}
