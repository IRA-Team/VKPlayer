package com.irateam.vkplayer.ui;

import android.graphics.Canvas;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.animation.Interpolator;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    public static final float ALPHA_FULL = 1.0f;

    private final ItemTouchHelperAdapter mAdapter;

    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Set movement flags based on the layout manager
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            final int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        } else {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }
    }

    private int mCachedMaxScrollSpeed = -1;
    private static final Interpolator sDragViewScrollCapInterpolator = t -> {
        t -= 1.0f;
        return t * t * t * t * t + 1.0f;
    };

    private static final Interpolator sDragScrollInterpolator = t -> t * t * t;

    private int getMaxDragScroll(RecyclerView recyclerView) {
        if (mCachedMaxScrollSpeed == -1) {
            mCachedMaxScrollSpeed = recyclerView.getResources().getDimensionPixelSize(
                    android.support.v7.recyclerview.R.dimen.item_touch_helper_max_drag_scroll_per_frame);
        }
        return mCachedMaxScrollSpeed;
    }

    @Override
    public int interpolateOutOfBoundsScroll(RecyclerView recyclerView,
                                            int viewSize,
                                            int viewSizeOutOfBounds,
                                            int totalSize,
                                            long msSinceStartScroll) {

        final int maxScroll = getMaxDragScroll(recyclerView);
        final int absOutOfBounds = Math.abs(viewSizeOutOfBounds);
        final int direction = (int) Math.signum(viewSizeOutOfBounds);
        // might be negative if other direction
        float outOfBoundsRatio = Math.min(1f, 1f * absOutOfBounds / viewSize);
        final int cappedScroll = (int) (direction * maxScroll *
                sDragViewScrollCapInterpolator.getInterpolation(outOfBoundsRatio));

        final float timeRatio;
        if (msSinceStartScroll < 2000) {
            timeRatio = 0.6f;
        } else if (msSinceStartScroll < 4000){
            timeRatio = 0.9f;
        } else {
            timeRatio = 1.2f;
        }
        final int value = (int) (cappedScroll * sDragScrollInterpolator
                .getInterpolation(timeRatio));
        if (value == 0) {
            return viewSizeOutOfBounds > 0 ? 1 : -1;
        }
        return value;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        if (source.getItemViewType() != target.getItemViewType()) {
            return false;
        }

        // Notify the adapter of the move
        mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        // Notify the adapter of the dismissal
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Fade out the view as it is swiped out of the parent's bounds
            final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        // We only want the active item to change
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof ItemTouchHelperViewHolder) {
                // Let the view holder know that this item is being moved or dragged
                ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
                itemViewHolder.onItemSelected();
            }
        }

        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        viewHolder.itemView.setAlpha(ALPHA_FULL);

        if (viewHolder instanceof ItemTouchHelperViewHolder) {
            // Tell the view holder it's time to restore the idle state
            ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
            itemViewHolder.onItemClear();
        }
    }
}
