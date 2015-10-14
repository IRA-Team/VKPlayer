package com.irateam.vkplayer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vk.sdk.api.model.VKApiAudio;

public abstract class DownloadFinishedReceiver extends BroadcastReceiver {
    public static final String AUDIO_ID = "audio_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        VKApiAudio audio = intent.getParcelableExtra(AUDIO_ID);
        onDownloadFinished(audio);
    }

    public abstract void onDownloadFinished(VKApiAudio audio);
}
