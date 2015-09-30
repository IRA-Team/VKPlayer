package com.irateam.vkplayer.serveces;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AudioService {

    public List<VKApiAudio> getMyAudio() {
        final List<VKApiAudio> list = new ArrayList<>();
        VKApi.audio().get().executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONArray array = response.json.getJSONObject("response").getJSONArray("items");
                    for (int i = 0; i < array.length(); i++)
                        list.add(new VKApiAudio().parse(array.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return list;
    }

    public List<VKApiAudio> getRecommendationAudio() {
        final List<VKApiAudio> list = new ArrayList<>();
        VKApi.audio().getRecommendations().executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONArray array = response.json.getJSONObject("response").getJSONArray("items");
                    for (int i = 0; i < array.length(); i++)
                        list.add(new VKApiAudio().parse(array.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return list;
    }

}
