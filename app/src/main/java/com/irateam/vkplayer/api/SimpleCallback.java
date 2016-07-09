package com.irateam.vkplayer.api;

public class SimpleCallback<T> implements Callback<T> {

    private SuccessListener<T> successListener;
    private ErrorListener errorListener;
    private FinishListener finishListener;

    protected SimpleCallback() {

    }

    public static <C> SimpleCallback<C> success(SuccessListener<C> listener) {
        SimpleCallback<C> callback = new SimpleCallback<>();
        callback.successListener = listener;
        return callback;
    }

    public SimpleCallback<T> error(ErrorListener errorListener) {
        this.errorListener = errorListener;
        return this;
    }

    public SimpleCallback<T> finish(FinishListener finishListener) {
        this.finishListener = finishListener;
        return this;
    }

    @Override
    public void onComplete(T result) {
        notifySuccess(result);
        notifyFinish();
    }

    @Override
    public void onError() {
        notifyError();
        notifyFinish();
    }

    private void notifySuccess(T result) {
        if (successListener != null) {
            successListener.onSuccess(result);
        }
    }

    private void notifyError() {
        if (errorListener != null) {
            errorListener.onError();
        }
    }

    private void notifyFinish() {
        if (finishListener != null) {
            finishListener.onFinish();
        }
    }

    public interface SuccessListener<T> {

        void onSuccess(T result);
    }

    public interface ErrorListener {

        void onError();
    }

    public interface FinishListener {

        void onFinish();
    }
}
