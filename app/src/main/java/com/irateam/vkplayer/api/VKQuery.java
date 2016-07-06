package com.irateam.vkplayer.api;

import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

public abstract class VKQuery<T> extends AbstractQuery<T> {

    private final VKRequest request;

    public VKQuery(VKRequest request) {
        this.request = request;
    }

    @Override
    public T execute() throws RuntimeException {
        VKSyncListener listener = new VKSyncListener();
        request.executeSyncWithListener(listener);
        return listener.result;
    }

    @Override
    public void execute(Callback<T> callback) {
        request.executeWithListener(new VKCallbackAdapter(callback));
    }

    @Override
    public void cancel() {
        request.cancel();
    }

    @Override
    protected T query() {
        throw new UnsupportedOperationException("VK query doesn't have get method");
    }

    protected abstract T parse(VKResponse response);

    private class VKCallbackAdapter extends VKRequest.VKRequestListener {

        private final Callback<T> callback;

        public VKCallbackAdapter(Callback<T> callback) {
            this.callback = callback;
        }

        @Override
        public void onComplete(VKResponse response) {
            T result = parse(response);
            callback.onComplete(result);
        }

        @Override
        public void onError(VKError error) {
            callback.onError();
        }
    }

    private class VKSyncListener extends VKRequest.VKRequestListener {

        private T result;

        @Override
        public void onComplete(VKResponse response) {
            result = parse(response);
        }

        @Override
        public void onError(VKError error) {
            result = null;
        }
    }
}
