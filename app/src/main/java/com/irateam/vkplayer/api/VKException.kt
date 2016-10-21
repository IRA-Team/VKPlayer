package com.irateam.vkplayer.api

import com.vk.sdk.api.VKError

class VKException : RuntimeException {

	val error: VKError?

	constructor(error: VKError?) : super(error?.errorMessage) {
		this.error = error
	}
}