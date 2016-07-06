package com.irateam.vkplayer.api;

public class SimpleCallback<T> implements Callback<T> {

    private SuccessListener<T> successListener;
    private ErrorListener errorListener;

    protected SimpleCallback() {

    }

    public static <C> SimpleCallback<C> of(SuccessListener<C> listener) {
        SimpleCallback<C> callback = new SimpleCallback<>();
        callback.successListener = listener;
        return callback;
    }

    @Override
    public void onComplete(T result) {
        notifySuccess(result);
    }

    @Override
    public void onError() {
        notifyError();
    }

    public void notifySuccess(T result) {
        if (successListener != null) {
            successListener.onSuccess(result);
        }
    }

    public void notifyError() {
        if (errorListener != null) {
            errorListener.onError();
        }
    }

    public interface SuccessListener<T> {

        void onSuccess(T result);
    }

    public interface ErrorListener {

        void onError();
    }
}
