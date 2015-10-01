package com.irateam.vkplayer.services;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AudioService extends VKRequest.VKRequestListener {

    public static final String GENRE_ID = "genre_id";

    public void getMyAudio() {
        VKApi.audio().get().executeWithListener(this);
    }

    public void getRecommendationAudio() {
        VKApi.audio().getRecommendations().executeWithListener(this);
    }

    public void getPopularAudio() {
        VKApi.audio().getPopular(VKParameters.from(GENRE_ID, 0)).executeWithListener(this);
    }

    @Override
    public void onComplete(VKResponse response) {
        super.onComplete(response);
        try {
            List<VKApiAudio> list = new ArrayList<>();
            JSONArray array = response.json.getJSONObject("response").getJSONArray("items");
            for (int i = 0; i < array.length(); i++) {
                list.add(new VKApiAudio().parse(array.getJSONObject(i)));
            }
            notifyAllComplete(list);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("OK");
    }

    @Override
    public void onError(VKError error) {
        super.onError(error);
        System.out.println("ERROR");
    }

    private List<WeakReference<Listener>> listeners = new ArrayList<>();

    public interface Listener {
        void onComplete(List<VKApiAudio> list);
    }

    public void addListener(Listener listener) {
        listeners.add(new WeakReference<Listener>(listener));
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void notifyAllComplete(List<VKApiAudio> list) {
        for (WeakReference<Listener> l : listeners) {
            l.get().onComplete(list);
        }
    }

    private void notifyAllError() {

    }

}
