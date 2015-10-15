package com.irateam.vkplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.ui.AudioListElement;
import com.irateam.vkplayer.utils.AlbumCoverUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AudioAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private List<Audio> list = new ArrayList<>();
    private List<Integer> checkedList = new ArrayList<>();

    private boolean sortMode = false;

    public AudioAdapter(Context context) {
        this.context = context;
    }

    public List<Audio> getList() {
        return list;
    }

    public void setList(List<Audio> list) {
        this.checkedList = new ArrayList<>();
        this.list = list;
        originalList = list;
    }

    public void updateAudioById(Audio audio) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == audio.id) {
                list.set(i, audio);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void removeChecked() {
        for (int i : checkedList) {
            list.remove(i);
        }
        notifyDataSetChanged();
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

        final Audio audio = list.get(position);
        audio.artist = audio.artist.trim();

        element.setTitle(audio.title);
        element.setArtist(audio.artist);
        element.setCoverDrawable(AlbumCoverUtils.createFromAudio(audio));
        element.setCoverOnClickListener((v) -> {
            if (!sortMode) {
                notifyCoverChecked(position);
            }
        });

        element.setDuration(audio.duration);

        if (audio.isCached()) {
            element.setDownloaded(true);
        }
        if (!sortMode) {
            if (checkedList.contains(position)) {
                element.setChecked(true);
            }
        } else {
            element.setSorted(true);
        }
        return view;
    }

    public void toggleChecked(int position) {
        if (checkedList.contains(position)) {
            checkedList.remove(checkedList.indexOf(position));
        } else {
            checkedList.add(position);
        }
        notifyDataSetChanged();
    }

    public List<Integer> getCheckedIndexList() {
        return checkedList;
    }

    public List<Audio> getCheckedItems() {
        List<Audio> list = new ArrayList<>();
        for (Integer i : checkedList) {
            list.add(this.list.get(i));
        }
        return list;
    }

    public List<Audio> getCachedCheckedItems() {
        List<Audio> cachedAudios = new ArrayList<>();
        for (Audio audio : getCheckedItems()) {
            if (audio.isCached()) {
                cachedAudios.add(audio);
            }
        }
        return cachedAudios;
    }

    public List<Audio> getNotCachedItems() {
        List<Audio> notCachedAudios = new ArrayList<>();
        for (Audio audio : getCheckedItems()) {
            if (!audio.isCached()) {
                notCachedAudios.add(audio);
            }
        }
        return notCachedAudios;
    }

    public int getCheckedCount() {
        return checkedList.size();
    }

    public void clearChecked() {
        checkedList = new ArrayList<>();
        notifyDataSetChanged();
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

    List<Audio> originalList;

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Audio> resultList;
                if (constraint == "") {
                    resultList = originalList;
                } else {
                    String key = constraint.toString().trim();
                    resultList = new ArrayList<>();

                    for (Audio audio : originalList) {
                        if (audio.title.toLowerCase().contains(key) || audio.artist.toLowerCase().contains(key)) {
                            resultList.add(audio);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.count = resultList.size();
                filterResults.values = resultList;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list = (ArrayList<Audio>) results.values;
                notifyDataSetChanged();
            }
        };
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
