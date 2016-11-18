package com.irateam.vkplayer.util.extension

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.irateam.vkplayer.OnAfterTextChangedListener
import com.irateam.vkplayer.OnBeforeTextChangedListener
import com.irateam.vkplayer.OnTextChangedListener

fun EditText.addOnAfterTextChangedListener(afterTextChangedListener: OnAfterTextChangedListener) {
    addTextChangedListener(object : TextWatcherAdapter(){
        override fun afterTextChanged(s: Editable) {
            afterTextChangedListener(s)
        }
    })
}

fun EditText.addOnBeforeTextChangedListener(beforeTextChangedListener: OnBeforeTextChangedListener) {
    addTextChangedListener(object : TextWatcherAdapter(){
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            beforeTextChangedListener(s, start, count, after)
        }
    })
}

fun EditText.addOnTextChangedListener(onTextChangedListener: OnTextChangedListener) {
    addTextChangedListener(object : TextWatcherAdapter() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            onTextChangedListener(s, start, before, count)
        }
    })
}

open class TextWatcherAdapter : TextWatcher {

    override fun afterTextChanged(s: Editable) {

    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }
}