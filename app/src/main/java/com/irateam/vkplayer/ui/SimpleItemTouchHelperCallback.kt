package com.irateam.vkplayer.ui

import android.graphics.Canvas
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.animation.Interpolator

class SimpleItemTouchHelperCallback(private val mAdapter: ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {

	override fun isLongPressDragEnabled(): Boolean {
		return false
	}

	override fun isItemViewSwipeEnabled(): Boolean {
		return true
	}

	override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
		// Set movement flags based on the layout manager
		if (recyclerView.layoutManager is GridLayoutManager) {
			val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
			val swipeFlags = 0
			return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
		} else {
			val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
			val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
			return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
		}
	}

	private var mCachedMaxScrollSpeed = -1

	private fun getMaxDragScroll(recyclerView: RecyclerView): Int {
		if (mCachedMaxScrollSpeed == -1) {
			mCachedMaxScrollSpeed = recyclerView.resources.getDimensionPixelSize(
					android.support.v7.recyclerview.R.dimen.item_touch_helper_max_drag_scroll_per_frame)
		}
		return mCachedMaxScrollSpeed
	}

	override fun interpolateOutOfBoundsScroll(recyclerView: RecyclerView,
											  viewSize: Int,
											  viewSizeOutOfBounds: Int,
											  totalSize: Int,
											  msSinceStartScroll: Long): Int {

		val maxScroll = getMaxDragScroll(recyclerView)
		val absOutOfBounds = Math.abs(viewSizeOutOfBounds)
		val direction = Math.signum(viewSizeOutOfBounds.toFloat()).toInt()
		// might be negative if other direction
		val outOfBoundsRatio = Math.min(1f, 1f * absOutOfBounds / viewSize)
		val cappedScroll = (direction.toFloat() * maxScroll.toFloat() *
				sDragViewScrollCapInterpolator.getInterpolation(outOfBoundsRatio)).toInt()

		val timeRatio: Float
		if (msSinceStartScroll < 2000) {
			timeRatio = 0.6f
		} else if (msSinceStartScroll < 4000) {
			timeRatio = 0.9f
		} else {
			timeRatio = 1.2f
		}
		val value = (cappedScroll * sDragScrollInterpolator.getInterpolation(timeRatio)).toInt()
		if (value == 0) {
			return if (viewSizeOutOfBounds > 0) 1 else -1
		}
		return value
	}

	override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
		if (source.itemViewType != target.itemViewType) {
			return false
		}

		// Notify the adapter of the move
		mAdapter.onItemMove(source.adapterPosition, target.adapterPosition)
		return true
	}

	override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
		// Notify the adapter of the dismissal
		mAdapter.onItemDismiss(viewHolder.adapterPosition)
	}

	override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
			// Fade out the view as it is swiped out of the parent's bounds
			val alpha = ALPHA_FULL - Math.abs(dX) / viewHolder.itemView.width.toFloat()
			viewHolder.itemView.alpha = alpha
			viewHolder.itemView.translationX = dX
		} else {
			super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
		}
	}

	override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
		// We only want the active item to change
		if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
			if (viewHolder is ItemTouchHelperViewHolder) {
				// Let the view holder know that this item is being moved or dragged
				viewHolder.onItemSelected()
			}
		}

		super.onSelectedChanged(viewHolder, actionState)
	}

	override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder) {
		super.clearView(recyclerView, viewHolder)

		viewHolder.itemView.alpha = ALPHA_FULL

		if (viewHolder is ItemTouchHelperViewHolder) {
			// Tell the view holder it's time to restore the idle state
			viewHolder.onItemClear()
		}
	}

	companion object {

		val ALPHA_FULL = 1.0f
		private val sDragViewScrollCapInterpolator = Interpolator{
			val t = it - 1.0f
			t * t * t * t * t + 1.0f
		}

		private val sDragScrollInterpolator = Interpolator{ t -> t * t * t }
	}
}
