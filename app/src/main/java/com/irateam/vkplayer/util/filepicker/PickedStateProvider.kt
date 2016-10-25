package com.irateam.vkplayer.util.filepicker

import java.io.File

interface PickedStateProvider {

	fun getPickedFiles(): Collection<File>
}