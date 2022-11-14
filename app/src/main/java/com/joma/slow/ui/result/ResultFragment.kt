package com.joma.slow.ui.result

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.joma.slow.base.BaseFragment
import com.joma.slow.databinding.FragmentResultBinding
import com.joma.slow.ui.custom.RateDialog

class ResultFragment : BaseFragment<FragmentResultBinding>(FragmentResultBinding::inflate) {

    lateinit var player: ExoPlayer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller.popBackStack()

        val path = arguments?.getString("path")!!

        player = ExoPlayer.Builder(requireContext()).build()
        binding.video.player = player

        RateDialog(requireContext())

        val mediaItem = MediaItem.fromUri(path.toUri())
        player.setMediaItem(mediaItem)
        player.prepare()
    }
}