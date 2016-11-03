/*
 * Copyright (C) 2015 IRA-Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irateam.vkplayer.controller

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.model.Audio
import com.irateam.vkplayer.player.*
import com.irateam.vkplayer.player.Player.RepeatState.*
import com.irateam.vkplayer.util.extension.*
import org.greenrobot.eventbus.Subscribe

/*
* Class that observe player service for playback events and controls it with ui
*/
open class PlayerController {

	protected val context: Context
	protected val resources: Resources

	/*
	* Views
	*/
	val rootView: View

	val songName: TextView
	val author: TextView

	val repeat: ImageView
	val previous: ImageView
	val playPause: ImageView
	val next: ImageView
	val random: ImageView

	val progress: SeekBar

	/*
	* Flag that indicates user holds progress bar
	*/
	var dragMode: Boolean = false

	private val resumeAction: (View) -> Unit = { Player.resume() }
	private val pauseAction: (View) -> Unit = { Player.pause(true) }


	/*
	* Constructor accepts 2 parameters:
	* Context for access to resources
	* Root view that contains views for playback controls
	*/
	constructor(context: Context, view: View) {
		this.context = context
		resources = context.resources

		rootView = view

		songName = rootView.getViewById(R.id.player_panel_song_name)
		author = rootView.getViewById(R.id.player_panel_author)

		repeat = rootView.getViewById(R.id.player_panel_repeat)
		previous = rootView.getViewById(R.id.player_panel_previous)
		playPause = rootView.getViewById(R.id.player_panel_play_pause)
		next = rootView.getViewById(R.id.player_panel_next)
		random = rootView.getViewById(R.id.player_panel_random)

		progress = rootView.getViewById(R.id.progress)
	}

	/*
	* Configuring view events when playerService is set up
	*/
	open fun initialize() {
		configurePanel()

		if (Player.isPlaying) setupPlay() else setupPause()

		previous.setOnClickListener { Player.previous() }
		next.setOnClickListener { Player.next() }

		setRepeatState(Player.repeatState)
		repeat.setOnClickListener { Player.switchRepeatState() }

		setRandomState(Player.randomState)
		random.setOnClickListener { Player.switchRandomState() }

		if (Player.isReady && !Player.isPlaying) {
			progress.progress = Player.pauseTime
		}

		progress.setOnSeekBarChangeListener(ProgressBarChangeListener())
	}

	open fun getPlayDrawable(): Drawable {
		return context.getThemedDrawable(R.drawable.ic_player_play_grey_18dp)
	}

	open fun getPauseDrawable(): Drawable {
		return context.getThemedDrawable(R.drawable.ic_player_pause_grey_18dp)
	}

	open fun setupPlay() {
		playPause.setImageDrawable(getPauseDrawable())
		playPause.setOnClickListener(pauseAction)
	}

	open fun setupPause() {
		playPause.setImageDrawable(getPlayDrawable())
		playPause.setOnClickListener(resumeAction)
	}

	fun configurePanel() {
		val audio = Player.audio
		val index = Player.audioPosition

		if (audio != null) {
			rootView.visibility = View.VISIBLE
			setAudio(index, audio)
			setRepeatState(Player.repeatState)
			setRandomState(Player.randomState)
		}
	}

	fun show(animationListener: (() -> Unit)? = null) {
		rootView.slideInUp(animationListener)
	}

	fun hide() {
		rootView.slideOutDown()
	}

	fun isVisible(): Boolean {
		return rootView.isVisible
	}

	@Subscribe
	open fun onPlayEvent(e: PlayerPlayEvent) {
		val index = e.index
		val audio = e.audio

		setAudio(index, audio)
		setupPlay()
	}

	@Subscribe
	open fun onPauseEvent(e: PlayerPauseEvent) {
		setupPause()
	}

	@Subscribe
	open fun onResumeEvent(e: PlayerResumeEvent) {
		setupPlay()
	}

	@Subscribe
	open fun onProgressChangedEvent(e: PlayerProgressChangedEvent) {
		setProgress(e.milliseconds)
	}

	@Subscribe
	open fun onBufferingUpdate(e: PlayerBufferingUpdateEvent) {
		progress.secondaryProgress = e.milliseconds
	}

	@Subscribe
	open fun onPlayerRandomChangedEvent(e: PlayerRandomChangedEvent) {
		setRandomState(e.randomState)
		v(TAG, "Random state changed to ${e.randomState}")
	}

	@Subscribe
	open fun onPlayerRepeatChangedEvent(e: PlayerRepeatChangedEvent) {
		setRepeatState(e.repeatState)
		v(TAG, "Repeat state changed to ${e.repeatState}")
	}

	open fun setAudio(index: Int, audio: Audio?) = audio?.let {
		val position = (index + 1)

		if (!isVisible()) {
			show()
		}

		songName.text = if (position != 0) {
			"$position. ${audio.title}"
		} else {
			audio.title
		}
		author.text = audio.artist
		progress.max = audio.duration * 1000
		progress.progress = 0
		progress.secondaryProgress = 0
	}

	open fun setProgress(milliseconds: Int) {
		if (!dragMode) {
			progress.progress = milliseconds
		}
	}

	fun setRepeatState(repeatState: Player.RepeatState) {
		@DrawableRes val drawableRes = when (repeatState) {
			NO_REPEAT  -> R.drawable.ic_player_repeat_light_grey_18dp
			ALL_REPEAT -> R.drawable.ic_player_repeat_all_light_grey_18dp
			ONE_REPEAT -> R.drawable.ic_player_repeat_one_light_grey_18dp
		}

		repeat.setImageDrawable(context.getThemedDrawable(drawableRes))
	}

	fun setRandomState(randomState: Boolean) {
		@DrawableRes val drawableRes = if (randomState) {
			R.drawable.ic_player_random_on_light_grey_18dp
		} else {
			R.drawable.ic_player_random_light_grey_18dp
		}
		random.setImageDrawable(context.getThemedDrawable(drawableRes))
	}

	protected inner class ProgressBarChangeListener : SeekBar.OnSeekBarChangeListener {

		override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
		}

		override fun onStartTrackingTouch(seekBar: SeekBar) {
			dragMode = true
		}

		override fun onStopTrackingTouch(seekBar: SeekBar) {
			dragMode = false
			Player.seekTo(progress.progress)
		}
	}

	interface VisibilityController {

		fun showPlayerController()

		fun hidePlayerController()
	}

	companion object {

		val TAG: String = PlayerController::class.java.name
	}
}
