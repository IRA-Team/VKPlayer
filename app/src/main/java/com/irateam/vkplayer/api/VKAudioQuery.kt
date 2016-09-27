package com.irateam.vkplayer.api

import com.irateam.vkplayer.model.VKAudio
import com.vk.sdk.api.VKRequest
import com.vk.sdk.api.VKResponse
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class VKAudioQuery(request: VKRequest) : VKQuery<List<VKAudio>>(request) {

    override fun parse(response: VKResponse): List<VKAudio> {
        val list = ArrayList<VKAudio>()
        val jsonResponse = response.json.optJSONObject("response")

        val array: JSONArray
        if (jsonResponse != null) {
            array = jsonResponse.getJSONArray("items")
        } else {
            array = response.json.getJSONArray("response")
        }

        for (i in 0..array.length() - 1) {
            list.add(parse(array.getJSONObject(i)))
        }

        return list
    }

    fun parse(from: JSONObject): VKAudio {
        return VKAudio(
                from.optString("id").toString(),
                from.optInt("owner_id"),
                from.optString("artist").trim(),
                from.optString("title").trim(),
                from.optInt("duration"),
                from.optString("url"),
                from.optInt("lyrics_id"),
                from.optInt("album_id"),
                from.optInt("genre_id"),
                from.optString("access_key"))
    }
}
