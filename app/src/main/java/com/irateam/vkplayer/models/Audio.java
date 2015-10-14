package com.irateam.vkplayer.models;

import com.vk.sdk.api.model.VKApiAudio;

public class Audio extends VKApiAudio {
    public String cachePath;

    public boolean isCached() {
        return cachePath != null;
    }
}
