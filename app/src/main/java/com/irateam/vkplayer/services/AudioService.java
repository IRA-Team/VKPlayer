package com.irateam.vkplayer.services;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AudioService extends VKRequest.VKRequestListener {

    public static final String GENRE_ID = "genre_id";
    private VKRequest lastRequest;

    public void getMyAudio() {
        performRequest(VKApi.audio().get());
    }

    public void getRecommendationAudio() {
        performRequest(VKApi.audio().getRecommendations());
    }

    public void getPopularAudio() {
        performRequest(VKApi.audio().getPopular(VKParameters.from(GENRE_ID, 0)));
    }

    private void performRequest(VKRequest request) {
        lastRequest = request;
        request.executeWithListener(this);
    }

    public void repeatLastRequest() {
        lastRequest.executeWithListener(this);
    }

    @Override
    public void onComplete(VKResponse response) {
        super.onComplete(response);
        try {
            List<VKApiAudio> list = new ArrayList<>();

            //Popular audio doesn't have JSONArray items, so need to check
            JSONArray array;
            JSONObject jsonResponse = response.json.optJSONObject("response");
            if (jsonResponse != null) {
                array = jsonResponse.getJSONArray("items");
            } else {
                array = response.json.getJSONArray("response");
            }

            for (int i = 0; i < array.length(); i++) {
                list.add(new VKApiAudio().parse(array.getJSONObject(i)));
            }
            notifyAllComplete(list);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(VKError error) {
        super.onError(error);
        notifyAllError(error);
    }

    private List<WeakReference<Listener>> listeners = new ArrayList<>();

    public interface Listener {
        void onComplete(List<VKApiAudio> list);

        void onError(VKError error);
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

    private void notifyAllError(VKError error) {
        for (WeakReference<Listener> l : listeners) {
            l.get().onError(error);
        }
    }

}
