package com.irateam.vkplayer.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.irateam.vkplayer.R
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder
import java.util.*

class AudioRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 1;
    private val TYPE_AUDIO = 2;

    var data: List<Any> = ArrayList()

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is Audio -> TYPE_AUDIO
        else -> -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            else -> {
                val v = inflater.inflate(R.layout.player_list_element_layout, parent, false)
                return AudioViewHolder(v)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is AudioViewHolder -> {
            val audio = data[position] as Audio
            holder.setAudio(audio)
        }

        else -> {

        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}
