package com.irateam.vkplayer.api

interface Query<T> {

    @Throws(RuntimeException::class)
    fun execute(): T

    fun execute(callback: Callback<T>)

    fun cancel()
}
