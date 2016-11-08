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

package com.irateam.vkplayer.util.extension

import android.view.View
import android.view.animation.Animation
import com.irateam.vkplayer.R

fun View.slideInUp(animationListener: (() -> Unit)? = null) {
	if (!isVisible) {
		isVisible = true
	}

	val slideIn = context.getAnimation(R.anim.default_slide_in_up)
	animationListener?.let {
		slideIn.setAnimationListener(object : Animation.AnimationListener {
			override fun onAnimationRepeat(p0: Animation?) = Unit

			override fun onAnimationEnd(p0: Animation?) = it.invoke()

			override fun onAnimationStart(p0: Animation?) = Unit
		})
	}

	startAnimation(slideIn)
}

fun View.slideInDown() {
	if (!isVisible) {
		isVisible = true
	}

	val slideIn = context.getAnimation(R.anim.default_slide_in_down)
	startAnimation(slideIn)
}

fun View.slideOutDown() {
	if (isVisible) {
		isVisible = false
	}

	val slideOut = context.getAnimation(R.anim.default_slide_out_down)
	startAnimation(slideOut)
}

fun View.slideOutUp() {
	val slideOut = context.getAnimation(R.anim.default_slide_out_up)
	startAnimation(slideOut)

	if (isVisible) {
		isVisible = false
	}
}

fun View.flipIn() {
	val flipIn = context.getAnimation(R.anim.flip_in_checked_overlay)
	flipIn.duration = 200
	startAnimation(flipIn)
}

fun View.flipOut() {
	val flipOut = context.getAnimation(R.anim.flip_out_checked_overlay)
	flipOut.duration = 100
	flipOut.repeatMode = Animation.REVERSE
	startAnimation(flipOut)
}

fun View.alphaOut() {
	val alphaOut = context.getAnimation(R.anim.default_alpha_out)
	alphaOut.duration = 200
	startAnimation(alphaOut)
}