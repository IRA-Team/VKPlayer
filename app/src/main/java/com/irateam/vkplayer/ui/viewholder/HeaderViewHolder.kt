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

package com.irateam.vkplayer.ui.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.model.Header
import com.irateam.vkplayer.ui.ItemTouchHelperViewHolder

/**
 * @author Artem Glugovsky
 */
class HeaderViewHolder : RecyclerView.ViewHolder, ItemTouchHelperViewHolder {

    val title: TextView

    constructor(v: View) : super(v) {
        title = v.findViewById(R.id.title) as TextView
    }

    fun setHeader(header: Header) {
        title.text = header.title
    }

    override fun onItemSelected() {
    }

    override fun onItemClear() {
    }
}