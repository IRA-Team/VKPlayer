package com.irateam.vkplayer.api;

public interface Query<T> {

    T execute() throws RuntimeException;

    void execute(Callback<T> callback);

    void cancel();
}
