package com.irateam.vkplayer.util.filepicker

import java.io.File

interface FileMatcher {

	fun match(file: File): Boolean

	class ExtensionMatcher : FileMatcher {

		val extension: String

		constructor(extension: String) {
			this.extension = extension
		}

		override fun match(file: File): Boolean {
			return file.extension == extension
		}
	}

	class DirectoryMatcher : FileMatcher {

		override fun match(file: File): Boolean {
			return file.isDirectory
		}

	}
}