package com.irateam.vkplayer.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.irateam.vkplayer.R
import com.irateam.vkplayer.adapter.LanguageRecyclerAdapter
import com.irateam.vkplayer.model.Language
import com.irateam.vkplayer.util.Languages
import com.irateam.vkplayer.util.extension.getViewById

class LanguagePickerDialog : DialogFragment() {

    var onLanguagePickedListener: ((Language) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity.layoutInflater.inflate(
                R.layout.dialog_language_picker, null, false)

        val adapter = LanguageRecyclerAdapter()
        adapter.languages = Languages.getSupportedLanguages(activity)
        adapter.onLanguagePickedListener = { onLanguagePickedListener?.invoke(it) }

        val recycler: RecyclerView = view.getViewById(R.id.recycler_view)
        recycler.layoutManager = LinearLayoutManager(activity)
        recycler.adapter = adapter

        return AlertDialog.Builder(activity)
                .setTitle(R.string.title_select_language)
                .setView(view)
                .create()
    }

    companion object {
        val TAG: String = LanguagePickerDialog::class.java.name

        fun newInstance() = LanguagePickerDialog()
    }
}