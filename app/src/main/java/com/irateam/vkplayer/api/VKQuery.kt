package com.irateam.vkplayer.api

import com.vk.sdk.api.VKError
import com.vk.sdk.api.VKRequest
import com.vk.sdk.api.VKResponse

abstract class VKQuery<T> : AbstractQuery<T> {

	private val request: VKRequest

	constructor(request: VKRequest) : super() {
		this.request = request
	}

	@Throws(RuntimeException::class)
	override fun execute(): T {
		val listener = VKSyncListener()
		request.executeSyncWithListener(listener)
		val result = listener.result
		if (result != null) {
			return result
		} else {
			throw VKException(listener.error)
		}
	}

	override fun execute(callback: Callback<T>) {
		request.executeWithListener(VKCallbackAdapter(callback))
	}

	override fun cancel() {
		request.cancel()
	}

	override fun query(): T {
		throw UnsupportedOperationException("${javaClass.name} doesn't have query method")
	}

	protected abstract fun parse(response: VKResponse): T

	private inner class VKCallbackAdapter : VKRequest.VKRequestListener {

		private val callback: Callback<T>

		constructor(callback: Callback<T>) : super() {
			this.callback = callback
		}

		override fun onComplete(response: VKResponse) {
			callback.onComplete(parse(response))
		}

		override fun onError(error: VKError) {
			callback.onError()
		}
	}

	private inner class VKSyncListener : VKRequest.VKRequestListener() {

		var result: T? = null
		var error: VKError? = null

		override fun onComplete(response: VKResponse) {
			this.result = parse(response)
		}

		override fun onError(error: VKError) {
			this.result = null
			this.error = error
		}
	}
}
