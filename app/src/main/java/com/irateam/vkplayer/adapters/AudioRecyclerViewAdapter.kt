package com.irateam.vkplayer.adapters

import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.irateam.vkplayer.R
import com.irateam.vkplayer.models.Audio
import com.irateam.vkplayer.player.*
import com.irateam.vkplayer.ui.ItemTouchHelperAdapter
import com.irateam.vkplayer.ui.SimpleItemTouchHelperCallback
import com.irateam.vkplayer.ui.viewholder.AudioViewHolder
import org.greenrobot.eventbus.Subscribe
import java.util.*

class AudioRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        ItemTouchHelperAdapter {

    private val TYPE_HEADER = 1;
    private val TYPE_AUDIO = 2;
    private val player = Player.getInstance()

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
                val v = inflater.inflate(R.layout.player_list_element_layout, parent, false)
                return AudioViewHolder(v)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is AudioViewHolder -> {
            val audio = data[position] as Audio

            holder.setAudio(audio)

            val playingAudio = player.playingAudio
            if (audio.id == playingAudio?.id) {
                if (player.isReady) {
                    holder.setPlaying(player.isPlaying)
                } else {
                    holder.setPreparing(true)
                }
            }

            holder.setChecked(checkedAudios.contains(audio))
            holder.itemView.setOnClickListener {
                player.queue = audios
                player.play(audios.indexOf(audio))
            }
            holder.setOnCoverClickListener(View.OnClickListener {
                holder.toggleChecked()
                if (holder.isChecked()) checkedAudios.add(audio) else checkedAudios.remove(audio)
                notifyDataSetChanged()
            })
            holder.itemView.setOnTouchListener { v, e ->
                if (MotionEventCompat.getActionMasked(e) == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                }
                false
            }
        }
        else -> {

        }
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


    override fun getItemCount(): Int {
        return data.size
    }

    fun setAudios(audios: List<Audio>) {
        data.clear()
        data.addAll(audios)
        this.audios = audios
        notifyDataSetChanged()
    }

    @Subscribe
    fun onStartEvent(e: PlayerStartEvent) {
        findAndNotify(e)
    }

    @Subscribe
    fun onPlayEvent(e: PlayerPlayEvent) {
        findAndNotify(e)
    }

    @Subscribe
    fun onPauseEvent(e: PlayerPauseEvent) {
        findAndNotify(e)
    }

    private fun findAndNotify(e: PlayerEvent) {
        notifyDataSetChanged()
    }
}
