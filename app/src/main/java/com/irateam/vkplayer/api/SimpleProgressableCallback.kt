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

package com.irateam.vkplayer.api

class SimpleProgressableCallback<T, P> : SimpleCallback<T>, ProgressableCallback<T, P> {

    private var progressListener: ((P) -> Unit)? = null

    constructor(successListener: (T) -> Unit) : super(successListener)

    infix fun progress(progressListener: (P) -> Unit): SimpleProgressableCallback<T, P> {
        this.progressListener = progressListener
        return this
    }

    override fun onProgress(progress: P) {
        throw UnsupportedOperationException("not implemented")
    }
}