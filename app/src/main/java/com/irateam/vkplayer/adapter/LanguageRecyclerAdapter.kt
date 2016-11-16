package com.irateam.vkplayer.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.irateam.vkplayer.R
import com.irateam.vkplayer.model.Language
import com.irateam.vkplayer.ui.viewholder.LanguageViewHolder
import com.irateam.vkplayer.util.extension.layoutInflater

class LanguageRecyclerAdapter : RecyclerView.Adapter<LanguageViewHolder>() {

    var languages: List<Language> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onLanguagePickedListener: ((Language) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val v = parent.context.layoutInflater.inflate(R.layout.item_language, parent, false)
        return LanguageViewHolder(v)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languages[position]
        holder.bindCountry(language)
        holder.itemView.setOnClickListener {
            onLanguagePickedListener?.invoke(language)
        }
    }

    override fun getItemCount(): Int = languages.size
}