package com.irateam.vkplayer.fragment

import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import com.irateam.vkplayer.R

class SettingsFragment : BaseFragment() {

    @StringRes
    override fun getTitleRes(): Int {
        return R.string.navigation_drawer_settings
    }

    @LayoutRes
    override fun getLayoutRes(): Int {
        return R.layout.fragment_settings
    }

    companion object {

        @JvmStatic
        fun newInstance() = SettingsFragment()
    }
}