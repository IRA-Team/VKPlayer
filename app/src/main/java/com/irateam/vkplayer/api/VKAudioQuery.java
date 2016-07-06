package com.irateam.vkplayer.api;

import com.irateam.vkplayer.models.Audio;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VKAudioQuery extends VKQuery<List<Audio>> {

    public VKAudioQuery(VKRequest request) {
        super(request);
    }

    @Override
    protected List<Audio> parse(VKResponse response) {
        List<Audio> list = new ArrayList<>();
        try {
            JSONArray array;
            JSONObject jsonResponse = response.json.optJSONObject("response");
            if (jsonResponse != null) {
                array = jsonResponse.getJSONArray("items");
            } else {
                array = response.json.getJSONArray("response");
            }
            for (int i = 0; i < array.length(); i++) {
                list.add(parse(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Audio parse(JSONObject from) {
        return new Audio(
                from.optInt("id"),
                from.optInt("owner_id"),
                from.optString("artist"),
                from.optString("title"),
                from.optInt("duration"),
                from.optString("url"),
                from.optInt("lyrics_id"),
                from.optInt("album_id"),
                from.optInt("genre_id"),
                from.optString("access_key"));
    }
}
