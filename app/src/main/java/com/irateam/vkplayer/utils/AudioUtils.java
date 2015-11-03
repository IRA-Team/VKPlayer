package com.irateam.vkplayer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.irateam.vkplayer.models.Audio;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
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
