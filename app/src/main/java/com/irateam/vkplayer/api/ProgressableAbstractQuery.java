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

public abstract class ProgressableAbstractQuery<T, P> extends AbstractQuery<T> implements ProgressableQuery<T, P> {

    private ProgressableCallback<T, P> progressableCallback;

    @Override
    public void execute(ProgressableCallback<T, P> callback) {
        this.progressableCallback = callback;
        execute((Callback<T>) callback);
    }

    protected void notifyProgress(P progress) {
        if (progressableCallback != null) {
            UI_HANDLER.post(() -> {
                progressableCallback.onProgress(progress);
            });
        }
    }
}
