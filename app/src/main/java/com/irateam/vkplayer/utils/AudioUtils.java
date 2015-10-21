package com.irateam.vkplayer.utils;

import com.irateam.vkplayer.models.Audio;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AudioUtils {

    public static List<Audio> parseJSONResponseToList(VKResponse response) {
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
                list.add(new Audio().parse(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

}
