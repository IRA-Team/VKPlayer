package com.irateam.vkplayer.ui

interface ItemTouchHelperAdapter {

	fun onItemMove(from: Int, to: Int): Boolean

	fun onItemDismiss(position: Int)
}