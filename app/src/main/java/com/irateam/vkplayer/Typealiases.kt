package com.irateam.vkplayer

import android.text.Editable

//EditText listeners
typealias OnAfterTextChangedListener = (Editable) -> Unit
typealias OnBeforeTextChangedListener = (CharSequence, Int, Int, Int) -> Unit
typealias OnTextChangedListener = (CharSequence, Int, Int, Int) -> Unit