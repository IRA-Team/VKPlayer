package com.irateam.vkplayer.api

class SimpleCallback<T> : Callback<T> {

    private val successListener: (T) -> Unit
    private var errorListener: (() -> Unit)? = null
    private var finishListener: (() -> Unit)? = null

    constructor(successListener: (T) -> Unit) {
        this.successListener = successListener
    }

    infix fun error(errorListener: () -> Unit): SimpleCallback<T> {
        this.errorListener = errorListener
        return this
    }

    infix fun finish(finishListener: () -> Unit): SimpleCallback<T> {
        this.finishListener = finishListener
        return this
    }

    override fun onComplete(result: T) {
        notifySuccess(result)
        notifyFinish()
    }

    override fun onError() {
        notifyError()
        notifyFinish()
    }

    private fun notifySuccess(result: T) {
        successListener.invoke(result)
    }

    private fun notifyError() {
        errorListener?.invoke()
    }

    private fun notifyFinish() {
        finishListener?.invoke()
    }

}