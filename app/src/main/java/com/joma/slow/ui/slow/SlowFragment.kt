package com.joma.slow.ui.slow

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.joma.slow.base.BaseFragment
import com.joma.slow.databinding.FragmentSlowBinding
import com.joma.slow.model.MVideo
import com.joma.slow.ui.gallery.GalleryViewModel
import com.joma.slow.ui.utils.AdapterListener
import com.joma.slow.ui.utils.dpToPx
import java.io.File

class SlowFragment : BaseFragment<FragmentSlowBinding>(FragmentSlowBinding::inflate),
    AdapterListener {

    lateinit var adapter: TimeLineAdapter
    val viewModel: SlowViewModel by viewModels()
    var progressHandler = Handler()
    lateinit var progressRunnable: Runnable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uri = Uri.parse(arguments?.getString("path"))

//        val file = File(
//            Environment.getExternalStorageDirectory().toString() + "/" + "project.mp4"
//        )

        val player = ExoPlayer.Builder(requireContext()).build()
        binding.video.player = player

        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    progressHandler.post(progressRunnable)
                } else {
                    progressHandler.removeCallbacks(progressRunnable)
                }
            }
        })

        progressRunnable = Runnable {
            val progressBar = (player.currentPosition * (adapter.getSize() * dpToPx(
                requireContext(),
                80
            )) / player.duration).toInt()
            Log.e("-----$progressBar", "1")
//            binding.recycler.layoutManager?.scrollHorizontallyBy(progressBar, null, null)
            progressHandler.postDelayed(progressRunnable, 10)
        }

        adapter = TimeLineAdapter(this)
        binding.recycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recycler.adapter = adapter

        val metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
        binding.recycler.setPadding(metrics.widthPixels / 2, 0, metrics.widthPixels / 2, 0)
        viewModel.getPreviews(requireContext(), uri)
        viewModel.previews.observe(viewLifecycleOwner) {
            adapter.setList(it)
            val w = dpToPx(requireContext(), it.size * 80)
            Log.e("-----$w", "${it.size}")
        }
    }

    override fun click(pos: Int) {

    }
}