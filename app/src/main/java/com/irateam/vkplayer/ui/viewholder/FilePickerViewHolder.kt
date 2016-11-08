package com.irateam.vkplayer.ui.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.util.extension.getViewById

open class FilePickerViewHolder : RecyclerView.ViewHolder {

	val name: TextView
	val checkBox: CheckBox
	val checkBoxHolder: View

	constructor(v: View) : super(v) {
		this.name = v.getViewById(R.id.name)
		this.checkBox = v.getViewById(R.id.checkbox)
		this.checkBoxHolder = v.getViewById(R.id.checkbox_holder)
	}
}