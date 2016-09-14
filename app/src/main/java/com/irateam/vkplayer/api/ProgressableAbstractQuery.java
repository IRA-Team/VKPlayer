/*
 * Copyright (C) 2016 IRA-Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irateam.vkplayer.api;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class ProgressableAbstractQuery<T, P> implements ProgressableQuery<T, P> {

    private static final int THREAD_COUNT = 3;
    private static final ExecutorService EXECUTOR_SERVICE =
            Executors.newFixedThreadPool(THREAD_COUNT);
    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());

    private Future<T> task;
    private ProgressableCallback<T, P> progressableCallback;

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
        AsyncCallableAdapter<T> callable = new AsyncCallableAdapter<>(this, callback);
        task = EXECUTOR_SERVICE.submit(callable);
    }

    @Override
    public void execute(ProgressableCallback<T, P> callback) {
        this.progressableCallback = callback;
        execute((Callback<T>) callback);
    }

    @Override
    public void cancel() {
        task.cancel(true);
    }

    protected abstract T query() throws Exception;

    protected void notifyProgress(P progress) {
        if (progressableCallback != null) {
            UI_HANDLER.post(() -> {
                progressableCallback.onProgress(progress);
            });
        }
    }

    private static class AsyncCallableAdapter<V> implements Callable<V> {

        private final Query<V> query;
        private final Callback<V> callback;

        private AsyncCallableAdapter(Query<V> query, Callback<V> callback) {
            this.query = query;
            this.callback = callback;
        }

        @Override
        public V call() throws Exception {
            V result = null;
            try {
                result = query.execute();
                notifyComplete(result);
            } catch (Exception e) {
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
