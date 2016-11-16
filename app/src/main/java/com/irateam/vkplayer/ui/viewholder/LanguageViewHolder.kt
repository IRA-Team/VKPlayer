package com.irateam.vkplayer.ui.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.model.Language
import com.irateam.vkplayer.util.extension.getViewById

class LanguageViewHolder : RecyclerView.ViewHolder {

    private val name: TextView

    constructor(v: View) : super(v) {
        this.name = v.getViewById(R.id.name)
    }

    fun bindCountry(language: Language) {
        name.text = language.name
    }
}