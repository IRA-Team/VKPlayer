package com.irateam.vkplayer.ui

import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView

class CustomItemAnimator : DefaultItemAnimator() {

    override fun setSupportsChangeAnimations(supportsChangeAnimations: Boolean) {
        super.setSupportsChangeAnimations(false)
    }
}
