package com.irateam.vkplayer.ui.viewholder

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.irateam.vkplayer.R
import com.irateam.vkplayer.models.Audio

class AudioViewHolder : RecyclerView.ViewHolder {

    private val resources: Resources

    private val title: TextView
    private val artist: TextView
    private val duration: TextView
    private val downloaded: ImageView

    //Cover views
    private val cover: ImageView
    private val progressBar: ProgressBar
    private val coverWrapper: FrameLayout

    private var checked: Boolean = false

    var downloadedState: Boolean = false
    var coverDrawable: Drawable? = null

    constructor(v: View) : super(v) {
        resources = v.context.resources

        title = v.findViewById(R.id.player_list_element_song_name) as TextView
        artist = v.findViewById(R.id.player_list_element_author) as TextView
        duration = v.findViewById(R.id.player_list_element_duration) as TextView
        cover = v.findViewById(R.id.player_list_element_cover) as ImageView
        progressBar = v.findViewById(R.id.player_list_element_progress) as ProgressBar
        coverWrapper = v.findViewById(R.id.player_list_element_cover_wrapper) as FrameLayout
        downloaded = v.findViewById(R.id.player_list_element_downloaded) as ImageView
    }

    fun setAudio(audio: Audio) {
        title.text = audio.title
        artist.text = audio.artist
        duration.text = String.format("%02d:%02d", audio.duration / 60, audio.duration % 60)

        setCover(audio)
    }

    private fun setCover(audio: Audio) {
        val char = audio.artist[0].toString()
        val color = ColorGenerator.MATERIAL.getColor(audio.artist)
        val drawable = TextDrawable.builder().buildRound(char, color)
        cover.setImageDrawable(drawable)
    }

    fun isChecked(): Boolean {
        return checked
    }

    fun setChecked(checked: Boolean) {
        this.checked = checked
        if (checked) {
            val color = resources.getColor(R.color.player_list_element_checked_color)
            itemView.setBackgroundColor(color)

            val layers = arrayOfNulls<Drawable>(2)
            layers[0] = coverDrawable
            layers[1] = resources.getDrawable(R.drawable.player_list_element_check_overlay)

            cover.setImageDrawable(LayerDrawable(layers))
        } else {
            val color = resources.getColor(R.color.player_list_element_color)
            itemView.setBackgroundColor(color)

            cover.setImageDrawable(coverDrawable)
        }
    }

    fun toggleChecked() {
        checked = !checked
        setChecked(checked)
    }

    fun setPlaying(playing: Boolean) {
        val layers = arrayOfNulls<Drawable>(2)
        layers[0] = coverDrawable
        if (playing) {
            layers[1] = resources.getDrawable(R.drawable.player_list_element_play_overlay)
        } else {
            layers[1] = resources.getDrawable(R.drawable.player_list_element_pause_overlay)
        }
        cover.setImageDrawable(LayerDrawable(layers))
    }

    fun setPreparing(preparing: Boolean) {
        if (preparing) {
            val layers = arrayOfNulls<Drawable>(2)
            layers[0] = coverDrawable
            layers[1] = resources.getDrawable(R.drawable.player_list_element_overlay)
            cover.setImageDrawable(LayerDrawable(layers))
            progressBar.visibility = View.VISIBLE
        }
    }

    fun setDownloaded(downloadedState: Boolean) {
        this.downloadedState = downloadedState
        if (downloadedState) {
            downloaded.visibility = View.VISIBLE
        } else {
            downloaded.visibility = View.GONE
        }
    }

    fun setCoverOnClickListener(listener: View.OnClickListener) {
        coverWrapper.setOnClickListener(listener)
    }
}
