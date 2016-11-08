package com.irateam.vkplayer.util.extension

import java.io.File

fun File.allFiles(): List<File> {
	var res = listOf(this)
	val files = this.listFiles()
	if (files != null && files.isNotEmpty()) {
		res += files.map { it.allFiles() }.flatten()
	}
	return res;
}