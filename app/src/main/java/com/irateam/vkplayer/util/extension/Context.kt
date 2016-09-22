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

package com.irateam.vkplayer.util.extension

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.support.annotation.AnimRes
import android.support.annotation.AnimatorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.irateam.vkplayer.util.Permission

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

fun Context.getAnimation(@AnimRes id: Int): Animation {
    return AnimationUtils.loadAnimation(this, id)
}

fun Context.getAnimator(@AnimatorRes id: Int): Animator {
    return AnimatorInflater.loadAnimator(this, id)
}

@Suppress("unchecked_cast")
fun <T> Context.getSystemService(name: String): T {
    return getSystemService(name) as T
}

fun Context.getThemedDrawable(@DrawableRes resId: Int): Drawable {
    return ContextCompat.getDrawable(this, resId)
}

fun Context.showLongToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
}

fun Context.showLongToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun Context.isPermissionsGranted(vararg permissions: Permission): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(this, it.value) ==
                PackageManager.PERMISSION_GRANTED
    }
}