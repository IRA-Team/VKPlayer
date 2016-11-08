package com.irateam.vkplayer.fragment

import android.os.Bundle
import com.irateam.vkplayer.R
import com.irateam.vkplayer.api.Query
import com.irateam.vkplayer.model.VKAudio


class VKMyAudioFragment : VKAudioListFragment() {

    override lateinit var query: Query<List<VKAudio>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        query = audioService.getMy()
    }

    override fun getTitleRes(): Int {
        return R.string.navigation_drawer_my_audio
    }

    companion object {

        @JvmStatic
        fun newInstance() = VKMyAudioFragment()
    }
}