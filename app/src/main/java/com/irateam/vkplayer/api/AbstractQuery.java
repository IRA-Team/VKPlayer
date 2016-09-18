package com.irateam.vkplayer.api;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class AbstractQuery<T> implements Query<T> {

    private static final int THREAD_COUNT = 3;
    private static final ExecutorService EXECUTOR_SERVICE =
            Executors.newFixedThreadPool(THREAD_COUNT);
    protected static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());

    private Future<T> task;

    @Override
    public T execute() throws RuntimeException {
        try {
            task = EXECUTOR_SERVICE.submit(this::query);
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    @Override
    public void execute(Callback<T> callback) {
        AsyncCallableAdapter<T> callable = new AsyncCallableAdapter<T>(this, callback);
        task = EXECUTOR_SERVICE.submit(callable);
    }

    @Override
    public void cancel() {
        task.cancel(true);
    }

    protected abstract T query() throws Exception;

    private static class AsyncCallableAdapter<V> implements Callable<V> {

        private final AbstractQuery<V> query;
        private final Callback<V> callback;

        private AsyncCallableAdapter(AbstractQuery<V> query, Callback<V> callback) {
            this.query = query;
            this.callback = callback;
        }

        @Override
        public V call() throws Exception {
            V result = null;
            try {
                result = query.query();
                notifyComplete(result);
            } catch (Exception e) {
                e.printStackTrace();
                notifyError();
            }
            return result;
        }

        private void notifyComplete(V result) {
            if (callback != null) {
                UI_HANDLER.post(() -> callback.onComplete(result));
            }
        }

        private void notifyError() {
            if (callback != null) {
                UI_HANDLER.post(callback::onError);
            }
        }
    }

}
