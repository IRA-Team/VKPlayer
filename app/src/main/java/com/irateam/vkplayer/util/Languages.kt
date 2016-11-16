package com.irateam.vkplayer.util

import android.content.Context
import com.irateam.vkplayer.R
import com.irateam.vkplayer.model.Language

object Languages {

    fun getSupportedLanguages(context: Context): List<Language> {
        val codes = context.resources.getStringArray(R.array.language_codes)
        val names = context.resources.getStringArray(R.array.language_names)

        return codes.mapIndexed { i, code -> Language(code, names[i]) }
    }

    fun getLanguageByCode(context: Context, code: String): Language? {
        return getSupportedLanguages(context).find { it.code == code }
    }
}