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

@file:JvmName("ContextUtils")
package com.irateam.vkplayer.utils

import android.content.Context
import android.net.ConnectivityManager

fun Context.isNetworkAvailable(): Boolean {
    val connectivityService = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityService.activeNetworkInfo
    return networkInfo?.isConnectedOrConnecting ?: false
}

fun Context.isWifiNetworkAvailable(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
    return networkInfo != null && networkInfo.isConnectedOrConnecting
}