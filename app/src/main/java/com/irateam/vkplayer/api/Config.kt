package com.irateam.vkplayer.api

object Config {

    val SUPPORTED_FORMATS: List<String>
    val SCAN_DIRECTORIES: List<String>
    val EXTERNAL_DIRECTORIES: List<String>

    init {
        SUPPORTED_FORMATS = listOf(
                "mp3")

        SCAN_DIRECTORIES = listOf(
                "/sdcard")

        EXTERNAL_DIRECTORIES = listOf(
                "/sdcard/.vkontakte/cache/audio")
    }
}