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

package com.irateam.vkplayer.util.extension

import com.irateam.vkplayer.api.ProgressableQuery
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.api.SimpleCallback
import com.irateam.vkplayer.api.SimpleProgressableCallback

inline fun <reified T> Query<T>.execute(noinline block: SimpleCallback<T>.() -> Unit) {
    execute(SimpleCallback(block))
}

inline fun <reified T, reified P> ProgressableQuery<T, P>.execute(
        noinline block: SimpleProgressableCallback<T, P>.() -> Unit) {

    execute(SimpleProgressableCallback(block))
}

inline fun <reified T> callback() = SimpleCallback<T>()

inline fun <reified T> callback(noinline block: SimpleCallback<T>.() -> Unit) = SimpleCallback(block)