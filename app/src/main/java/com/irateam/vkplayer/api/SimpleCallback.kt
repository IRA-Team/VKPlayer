package com.irateam.vkplayer.api

open class SimpleCallback<T> : Callback<T> {

    private var successListener: ((T) -> Unit)? = null
    private var errorListener: (() -> Unit)? = null
    private var finishListener: (() -> Unit)? = null

    constructor()

    constructor(block: SimpleCallback<T>.() -> Unit) {
        block.invoke(this)
    }

    open fun onSuccess(successListener: ((T) -> Unit)): SimpleCallback<T> {
        this.successListener = successListener
        return this
    }

    open fun onError(errorListener: () -> Unit): SimpleCallback<T> {
        this.errorListener = errorListener
        return this
    }

    open fun onFinish(finishListener: () -> Unit): SimpleCallback<T> {
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
        successListener?.invoke(result)
    }

    private fun notifyError() {
        errorListener?.invoke()
    }

    private fun notifyFinish() {
        finishListener?.invoke()
    }

}