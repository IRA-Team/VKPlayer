package com.irateam.vkplayer;

import com.irateam.vkplayer.notifications.PlayerNotification;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.vk.sdk.VKSdk;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .build();
        ImageLoader.getInstance().init(config);
        PlayerNotification.init(this);
    }

}
