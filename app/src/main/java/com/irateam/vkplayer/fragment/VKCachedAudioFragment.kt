package com.irateam.vkplayer.fragment

import android.os.Bundle
import com.irateam.vkplayer.R
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.model.VKAudio


class VKCachedAudioFragment : VKAudioListFragment() {

    override lateinit var query: Query<List<VKAudio>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        query = audioService.getCached()
    }

    override fun getTitleRes(): Int {
        return R.string.navigation_drawer_downloaded
    }

    companion object {

        @JvmStatic
        fun newInstance() = VKCachedAudioFragment()
    }
}