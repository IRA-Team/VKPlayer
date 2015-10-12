package com.irateam.vkplayer.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.irateam.vkplayer.R;
import com.irateam.vkplayer.database.AudioDatabaseHelper;
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
    private Context context;
    private VKRequest lastRequest;
    private List<WeakReference<Listener>> listeners = new ArrayList<>();

    public AudioService(Context context) {
        this.context = context;
    }

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
        if (checkNetwork()) {
            request.executeWithListener(this);
        } else {
            notifyAllError(context.getString(R.string.error_no_internet_connection));
        }
    }

    public void repeatLastRequest() {
        performRequest(lastRequest);
    }

    @Override
    public void onComplete(VKResponse response) {
        super.onComplete(response);
        try {
            List<VKApiAudio> vkList = new ArrayList<>();

            //Popular audio doesn't have JSONArray items, so need to check
            JSONArray array;
            JSONObject jsonResponse = response.json.optJSONObject("response");
            if (jsonResponse != null) {
                array = jsonResponse.getJSONArray("items");
            } else {
                array = response.json.getJSONArray("response");
            }
            for (int i = 0; i < array.length(); i++) {
                vkList.add(new VKApiAudio().parse(array.getJSONObject(i)));
            }

            List<VKApiAudio> cachedList = new AudioDatabaseHelper(context).getAll();
            for (int i = 0; i < vkList.size(); i++) {
                for (VKApiAudio audio : cachedList) {
                    if (vkList.get(i).id == audio.id) {
                        vkList.set(i, audio);
                    }
                }
            }
            notifyAllComplete(vkList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean checkNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onError(VKError error) {
        super.onError(error);
        notifyAllError(context.getString(R.string.error_load));
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

    private void notifyAllError(String errorMessage) {
        for (WeakReference<Listener> l : listeners) {
            l.get().onError(errorMessage);
        }
    }

    public interface Listener {
        void onComplete(List<VKApiAudio> list);

        void onError(String errorMessage);
    }

}
