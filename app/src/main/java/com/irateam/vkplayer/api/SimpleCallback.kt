package com.irateam.vkplayer.api

import java.util.*

open class SimpleCallback<T> : Callback<T> {

	private val successListeners: ArrayList<((T) -> Unit)> = ArrayList();
	private val errorListeners: ArrayList<(() -> Unit)> = ArrayList();
	private val finishListeners: ArrayList<(() -> Unit)> = ArrayList();

	constructor()

	constructor(block: SimpleCallback<T>.() -> Unit) {
		block.invoke(this)
	}

	open fun onSuccess(successListener: ((T) -> Unit)): SimpleCallback<T> {
		successListeners.add(successListener)
		return this
	}

	open fun onError(errorListener: () -> Unit): SimpleCallback<T> {
		errorListeners.add(errorListener)
		return this
	}

	open fun onFinish(finishListener: () -> Unit): SimpleCallback<T> {
		finishListeners.add(finishListener)
		return this
	}

	/**
	 * Do not use this methods in constructor block.
	 * It is implementation of Callback interface.
	 */
	override fun onComplete(result: T) {
		notifySuccess(result)
		notifyFinish()
	}

	override fun onError() {
		notifyError()
		notifyFinish()
	}

	private fun notifySuccess(result: T) {
		successListeners.forEach { it.invoke(result) }
	}

	private fun notifyError() {
		errorListeners.forEach { it.invoke() }
	}

	private fun notifyFinish() {
		finishListeners.forEach { it.invoke() }
	}

}