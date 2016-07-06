package com.irateam.vkplayer.api;

public interface Callback<T> {

    void onComplete(T result);

    void onError();

}
