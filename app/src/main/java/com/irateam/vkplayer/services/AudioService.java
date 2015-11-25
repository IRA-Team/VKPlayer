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

package com.irateam.vkplayer.services;

import android.content.Context;
import android.os.AsyncTask;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.database.AudioDatabaseHelper;
import com.irateam.vkplayer.models.Audio;
import com.irateam.vkplayer.utils.AudioUtils;
import com.irateam.vkplayer.utils.NetworkUtils;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AudioService extends VKRequest.VKRequestListener {

    public static final String GENRE_ID = "genre_id";
    private Context context;
    private PlayerService playerService;
    private List<WeakReference<Listener>> listeners = new ArrayList<>();

    private VKRequest lastRequest;
    private Runnable lastQuery;
    private VKRequest lastSearchRequest;

    AudioDatabaseHelper helper;

    public AudioService(Context context) {
        this.context = context;
        helper = new AudioDatabaseHelper(context);
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void getCurrentAudio() {
        performQuery(() -> notifyAllComplete(playerService.getPlaylist()), null);
    }

    public void getMyAudio() {
        VKRequest request = VKApi.audio().get();
        performQuery(() -> request.executeWithListener(this), request);
    }

    public void getRecommendationAudio() {
        VKRequest request = VKApi.audio().getRecommendations();
        performQuery(() -> request.executeWithListener(this), request);
    }

    public void getPopularAudio() {
        VKRequest request = VKApi.audio().getPopular(VKParameters.from(GENRE_ID, 0));
        performQuery(() -> request.executeWithListener(this), request);
    }

    public void getCachedAudio() {
        performQuery(() -> new GetFromCacheTask().execute(), null);
    }

    private void performQuery(Runnable query, VKRequest request) {
        lastQuery = query;

        if (lastRequest != null) {
            lastRequest.cancel();
        }

        if (request != null) {
            if (NetworkUtils.checkNetwork(context)) {
                query.run();
            } else {
                notifyAllError(context.getString(R.string.error_no_internet_connection));
            }
        } else {
            query.run();
        }
        lastRequest = request;

    }

    public void repeatLastRequest() {
        lastQuery.run();
    }

    @Override
    public void onComplete(VKResponse response) {
        super.onComplete(response);
        List<Audio> vkList = AudioUtils.parseJSONResponseToList(response);
        List<Audio> cachedList = new AudioDatabaseHelper(context).getAll();

        for (int i = 0; i < vkList.size(); i++) {
            for (Audio audio : cachedList) {
                if (vkList.get(i).equalsId(audio) && audio.isCached()) {
                    vkList.set(i, audio);
                }
            }
        }

        notifyAllComplete(vkList);
    }

    public void search(String query, Listener listener) {
        cancelSearch();
        lastSearchRequest = VKApi.audio().search(VKParameters.from(VKApiConst.Q, query));
        lastSearchRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                List<Audio> audios = AudioUtils.parseJSONResponseToList(response);
                listener.onComplete(audios);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                listener.onError(error.errorMessage);
            }
        });
    }

    public void cancelSearch() {
        if (lastSearchRequest != null) {
            lastSearchRequest.cancel();
        }
    }

    public void removeFromCache(List<Audio> cachedList, Listener listener) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                for (Audio audio : cachedList) {
                    if (audio.isCached()) {
                        helper.delete(audio);
                        audio.removeCacheFile();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                listener.onComplete(cachedList);
            }
        }.execute();
    }

    public void removeAllCachedAudio() {
        List<Audio> cachedList = helper.getAll();
        for (Audio audio : cachedList) {
            if (audio.isCached()) {
                audio.removeCacheFile();
            }
        }
        helper.removeAll();
    }

    @Override
    public void onError(VKError error) {
        super.onError(error);
        if (error.errorCode != VKError.VK_CANCELED) {
            notifyAllError(context.getString(R.string.error_load));
        }
    }

    public void addListener(Listener listener) {
        listeners.add(new WeakReference<Listener>(listener));
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void notifyAllComplete(List<Audio> list) {
        for (WeakReference<Listener> l : listeners) {
            l.get().onComplete(list);
        }
    }

    private void notifyAllError(String errorMessage) {
        for (WeakReference<Listener> l : listeners) {
            l.get().onError(errorMessage);
        }
    }

    public interface Listener {
        void onComplete(List<Audio> list);

        void onError(String errorMessage);
    }

    private class GetFromCacheTask extends AsyncTask<Void, Void, List<Audio>> {
        @Override
        protected List<Audio> doInBackground(Void... params) {
            List<Audio> list = new AudioDatabaseHelper(context).getAll();
            Iterator<Audio> i = list.iterator();
            while (i.hasNext()) {
                if (!i.next().isCached()) {
                    i.remove();
                }
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<Audio> audios) {
            super.onPostExecute(audios);
            notifyAllComplete(audios);
        }
    }
}
