package com.irateam.vkplayer.util.filepicker

import java.io.File

interface OnFilePickedStateChangeListener {

	fun onChange(file: File, isPicked: Boolean)
}