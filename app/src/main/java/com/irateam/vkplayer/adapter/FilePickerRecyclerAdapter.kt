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

package com.irateam.vkplayer.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.irateam.vkplayer.R
import com.irateam.vkplayer.ui.viewholder.DirectoryViewHolder
import com.irateam.vkplayer.ui.viewholder.FileViewHolder
import java.io.File

class FilePickerRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    var root: File
        set(value) {
            list = value.list()
                    .map { File(it) }
                    .sortedByDescending { it.isDirectory }
        }

    private var list = emptyList<File>()

    constructor() {
        root = File("/")
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].isDirectory) {
            TYPE_DIRECTORY
        } else {
            TYPE_FILE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_DIRECTORY) {
            DirectoryViewHolder(inflater.inflate(R.layout.item_directory, parent, false))
        } else {
            FileViewHolder(inflater.inflate(R.layout.item_file, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val file = list[position]
        when (holder) {
            is DirectoryViewHolder -> {
                holder.name.text = file.name
                holder.itemView.setOnClickListener {
                    root = file
                    notifyDataSetChanged()
                }
            }

            is FileViewHolder -> {
                holder.name.text = file.name
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private companion object {
        val TYPE_DIRECTORY = 1
        val TYPE_FILE = 2
    }
}