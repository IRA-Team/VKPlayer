package com.irateam.vkplayer.ui;

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int from, int to);

    void onItemDismiss(int position);
}