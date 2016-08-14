package com.irateam.vkplayer.api

interface Callback<T> {

    fun onComplete(result: T)

    fun onError()

}
