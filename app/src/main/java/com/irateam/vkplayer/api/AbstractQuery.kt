package com.irateam.vkplayer.api

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future

abstract class AbstractQuery<T> : Query<T> {

	private var task: Future<T>? = null

	@Throws(RuntimeException::class)
	override fun execute(): T = try {
		val task = EXECUTOR_SERVICE.submit<T> { this.query() }
		this.task = task
		task.get()
	} catch (e: InterruptedException) {
		throw e;
	} catch (e: ExecutionException) {
		throw e;
	}

	override fun execute(callback: Callback<T>) {
		val callable = AsyncCallableAdapter(this, callback)
		task = EXECUTOR_SERVICE.submit(callable)
	}

	override fun cancel() {
		task?.cancel(true)
	}

	@Throws(Exception::class)
	protected abstract fun query(): T

	private class AsyncCallableAdapter<V> : Callable<V> {

		private val query: AbstractQuery<V>
		private val callback: Callback<V>

		constructor(query: AbstractQuery<V>, callback: Callback<V>) {
			this.query = query
			this.callback = callback
		}

		@Throws(Exception::class)
		override fun call(): V = try {
			val result = query.query();
			notifyComplete(result)
			result
		} catch (e: Exception) {
			e.printStackTrace()
			notifyError()
			throw e;
		}

		private fun notifyComplete(result: V) {
			UI_HANDLER.post { callback.onComplete(result) }
		}

		private fun notifyError() {
			UI_HANDLER.post { callback.onError() }
		}
	}

	companion object {

		private val THREAD_COUNT = 3
		private val EXECUTOR_SERVICE = Executors.newFixedThreadPool(THREAD_COUNT)
		val UI_HANDLER = Handler(Looper.getMainLooper())
	}

}
