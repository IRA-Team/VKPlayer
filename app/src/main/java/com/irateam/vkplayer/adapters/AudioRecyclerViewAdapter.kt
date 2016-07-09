package com.irateam.vkplayer.adapters

import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import com.irateam.vkplayer.R
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.player.*
import com.irateam.vkplayer.ui.ItemTouchHelperAdapter
import com.irateam.vkplayer.ui.SimpleItemTouchHelperCallback
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder.State.*
import org.greenrobot.eventbus.Subscribe
import java.util.*

class AudioRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        ItemTouchHelperAdapter {

    private val TYPE_HEADER = 1;
    private val TYPE_AUDIO = 2;
    private val player = Player.getInstance()

    private var sortMode = false
    private val itemTouchHelper: ItemTouchHelper

    private var data = ArrayList<Any>()
    private var audios: List<Audio> = ArrayList()

    var checkedAudios: HashSet<Audio> = LinkedHashSet()

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is Audio -> TYPE_AUDIO
        else -> -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            else -> {
                val v = inflater.inflate(R.layout.item_audio, parent, false)
                return AudioViewHolder(v)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AudioViewHolder -> {
                val audio = data[position] as Audio

                configureAudio(holder, audio)
                configurePlayingState(holder, audio)
                configureCheckedState(holder, audio)
                configureSortMode(holder, audio)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(data, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    override fun onItemDismiss(position: Int) {
        data.remove(position)
        notifyItemRemoved(position);
    }

    private fun configureAudio(holder: AudioViewHolder, audio: Audio) {
        holder.setAudio(audio)
        holder.itemView.setOnClickListener {
            player.queue = audios
            player.play(audios.indexOf(audio))
        }
    }

    private fun configurePlayingState(holder: AudioViewHolder, audio: Audio) {
        val playingAudio = player.playingAudio
        if (audio.id == playingAudio?.id) {
            val state = when {
                !player.isReady -> PREPARE
                player.isReady && player.isPlaying -> PLAY
                player.isReady && !player.isPlaying -> PAUSE
                else -> NONE
            }
            holder.setPlayingState(state)
        }
    }

    private fun configureCheckedState(holder: AudioViewHolder, audio: Audio) {
        holder.setChecked(checkedAudios.contains(audio))
        holder.coverHolder.setOnClickListener {
            holder.toggleChecked()
            if (holder.isChecked()) checkedAudios.add(audio) else checkedAudios.remove(audio)
        }
    }

    private fun configureSortMode(holder: AudioViewHolder, audio: Audio) {
        holder.setSorting(sortMode)
        if (sortMode) {
            holder.coverHolder.setOnTouchListener { v, e ->
                if (MotionEventCompat.getActionMasked(e) == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                }
                false
            }
        }
    }

    fun setSortMode(enabled: Boolean) {
        sortMode = enabled
        notifyDataSetChanged()
    }

    fun setAudios(audios: List<Audio>) {
        data.clear()
        data.addAll(audios)
        this.audios = audios
        notifyDataSetChanged()
    }

    @Subscribe
    fun onStartEvent(e: PlayerStartEvent) {
        notifyEvent(e)
    }

    @Subscribe
    fun onPlayEvent(e: PlayerPlayEvent) {
        notifyEvent(e)
    }

    @Subscribe
    fun onResumeEvent(e: PlayerResumeEvent) {
        notifyEvent(e)
    }

    @Subscribe
    fun onPauseEvent(e: PlayerPauseEvent) {
        notifyEvent(e)
    }

    private fun notifyEvent(e: PlayerEvent) {
        notifyDataSetChanged()
    }
}
