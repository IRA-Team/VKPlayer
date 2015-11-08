/*
 * Copyright (C) 2015 IRA-Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irateam.vkplayer.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.player.Player;
import com.irateam.vkplayer.services.AudioService;
import com.irateam.vkplayer.services.PlayerService;
import com.irateam.vkplayer.ui.AudioListElement;
import com.irateam.vkplayer.utils.AlbumCoverUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AudioAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private PlayerService playerService;
    private AudioService audioService;

    private List<Audio> list = new ArrayList<>();
    private List<Audio> searchList = new ArrayList<>();
    private List<Integer> checkedList = new ArrayList<>();

    private boolean sortMode = false;
    private Player.PlayerEventListener playerEventListener;


    public AudioAdapter(Context context) {
        this.context = context;
        audioService = new AudioService(context);
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
        playerEventListener = (position, audio, event) -> notifyDataSetChanged();
        playerService.addPlayerEventListener(playerEventListener);
    }

    public int getPlayingAudioId() {
        if (playerService != null && playerService.getPlayingAudio() != null) {
            return playerService.getPlayingAudio().getId();
        } else {
            return -1;
        }
    }

    public List<Audio> getList() {
        return list;
    }

    public List<Audio> getListByPosition(int position) {
        return belongsToSearchList(position) ? searchList : list;
    }

    public boolean belongsToSearchList(int position) {
        return position > list.size();
    }

    public int getPosition(int position) {
        return belongsToSearchList(position) ? position - list.size() - 1 : position;
    }

    public void setList(List<Audio> list) {
        this.checkedList = new ArrayList<>();
        this.list = list;
        originalList = list;
        searchList = Collections.emptyList();
    }

    public void updateAudioById(Audio audio) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equalsId(audio)) {
                list.set(i, audio);
                break;
            }
        }

        for (int i = 0; i < searchList.size(); i++) {
            if (searchList.get(i).equalsId(audio)) {
                searchList.set(i, audio);
            }
        }
        notifyDataSetChanged();
    }

    public void updateAudiosById(List<Audio> audios) {
        for (int i = 0; i < list.size(); i++) {
            for (Audio audio : audios) {
                if (list.get(i).equalsId(audio)) {
                    list.set(i, audio);
                    break;
                }
            }
        }

        for (int i = 0; i < searchList.size(); i++) {
            for (Audio audio : audios) {
                if (searchList.get(i).equalsId(audio)) {
                    searchList.set(i, audio);
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    public void removeChecked() {
        for (Audio audio : getCheckedItems()) {
            list.remove(audio);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return searchList.size() > 0 ? list.size() + searchList.size() + 1 : list.size();
    }

    @Override
    public Object getItem(int position) {
        int listSize = list.size();
        if (position < listSize) {
            return list.get(position);
        } else if (position > listSize) {
            return searchList.get(position - listSize - 1);
        } else {
            return new Object();
        }
    }

    @Override
    public long getItemId(int position) {
        int listSize = list.size();
        if (position < listSize) {
            return list.get(position).getId();
        } else if (position > listSize) {
            return searchList.get(position - listSize - 1).getId();
        } else {
            return -1;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return position != list.size() && super.isEnabled(position);
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        int size = list.size();
        if (position != size) {
            view = inflater.inflate(R.layout.player_list_element, parent, false);

            AudioListElement element = (AudioListElement) view;
            Audio audio = (Audio) getItem(position);

            element.setTitle(audio.getTitle());
            element.setArtist(audio.getArtist());
            element.setCoverDrawable(AlbumCoverUtils.createFromAudio(audio));
            if (audio.getId() == getPlayingAudioId()) {
                if (playerService.isReady()) {
                    element.setPlaying(playerService.isPlaying());
                } else {
                    element.setPreparing(true);
                }
            }
            element.setCoverOnClickListener((v) -> {
                if (!sortMode) {
                    notifyCoverChecked(position);
                }
            });

            element.setDuration(audio.getDuration());

            if (audio.isCached()) {
                element.setDownloaded(true);
            }

            if (sortMode) {
                element.setSorted(true);
            } else if (checkedList.contains(position)) {
                element.setChecked(true);
            }
        } else {
            view = inflater.inflate(R.layout.player_list_subheader, parent, false);
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

    public List<Audio> getCheckedItems() {
        List<Audio> list = new ArrayList<>();
        for (Integer i : checkedList) {
            list.add((Audio) getItem(i));
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
        notifySortMode(sortMode);
    }

    List<Audio> originalList = new ArrayList<>();

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Audio> resultList = new ArrayList<>();

                audioService.cancelSearch();
                searchList = Collections.emptyList();
                if (constraint.toString().isEmpty()) {
                    resultList = originalList;
                } else {
                    String key = constraint.toString().trim();
                    for (Audio audio : originalList) {
                        if (audio.getTitle().toLowerCase().contains(key) || audio.getArtist().toLowerCase().contains(key)) {
                            resultList.add(audio);
                        }
                    }
                    audioService.search(key, new AudioService.Listener() {
                        @Override
                        public void onComplete(List<Audio> list) {
                            searchList = list;
                            new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());

                        }

                        @Override
                        public void onError(String errorMessage) {

                        }
                    });
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

    public void drop(int from, int to) {
        Audio audio = (Audio) getItem(from);
        if (!belongsToSearchList(from)) {
            list.remove(from);
        } else {
            searchList.remove(getPosition(from));
        }

        if (!belongsToSearchList(to)) {
            list.add(to, audio);
        } else {
            searchList.add(getPosition(to), audio);
        }
        notifyDataSetChanged();
    }

    //Cover check listener
    public interface CoverCheckListener {
        void onCoverCheck(int position);
    }

    private CoverCheckListener coverCheckListener;

    public void setCoverCheckListener(CoverCheckListener listener) {
        this.coverCheckListener = listener;
    }

    public void notifyCoverChecked(int position) {
        if (coverCheckListener != null) {
            coverCheckListener.onCoverCheck(position);
        }
    }


    //Sort listener
    private SortModeListener sortListeners;

    public void setSortModeListener(SortModeListener listener) {
        sortListeners = listener;
    }

    public void notifySortMode(boolean sortMode) {
        if (sortListeners != null) {
            if (sortMode) {
                sortListeners.onStartSortMode();
            } else {
                sortListeners.onFinishSortMode();
            }
        }
    }

    public interface SortModeListener {
        void onStartSortMode();

        void onFinishSortMode();
    }
}
