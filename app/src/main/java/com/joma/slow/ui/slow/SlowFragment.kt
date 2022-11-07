package com.joma.slow.ui.slow

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.joma.slow.R
import com.joma.slow.base.BaseFragment
import com.joma.slow.databinding.FragmentSlowBinding
import com.joma.slow.model.MVideo
import com.joma.slow.ui.utils.AdapterListener
import com.joma.slow.ui.utils.dpToPx
import com.joma.slow.ui.utils.millisecondsToTime


class SlowFragment : BaseFragment<FragmentSlowBinding>(FragmentSlowBinding::inflate){

    val viewModel: SlowViewModel by viewModels()
    var progressHandler = Handler()
    lateinit var progressRunnable: Runnable
    var timeLineList: MutableList<MVideo> = ArrayList()
    var scrolling = false

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
            val progressBar = (player.currentPosition * (timeLineList.size * dpToPx(
                requireContext(),
                80
            )) / player.duration).toInt()
            if (!scrolling) {
                binding.timelineParent.scrollTo(progressBar, 0)
            }
            progressHandler.postDelayed(progressRunnable, 10)
        }

        binding.timelineParent.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN ->{
                    scrolling = true
                }
                MotionEvent.ACTION_UP ->{
                    val seek = binding.timelineParent.scrollX.toFloat() / (timeLineList.size.toFloat() * dpToPx(
                        requireContext(),
                        80
                    ).toFloat()) * player.duration.toFloat()
                    player.seekTo(seek.toLong())
                    scrolling = false
                }
            }
            false
        }

        val metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
        binding.timelineParent.setPadding(metrics.widthPixels / 2, 0, metrics.widthPixels / 2, 0)
        viewModel.getPreviews(requireContext(), uri)
        viewModel.previews.observe(viewLifecycleOwner) { list ->
            timeLineList.clear()
            timeLineList.addAll(list)
            bindTimeLineItems()
        }
    }

    override fun onDetach() {
        progressHandler.removeCallbacks(progressRunnable)
        super.onDetach()
    }

    private fun bindTimeLineItems(){
        timeLineList.forEach {
            val item = layoutInflater.inflate(R.layout.item_time_line, null)
            item.findViewById<ImageView>(R.id.thumbnail).setImageBitmap(it.thumb)
            item.findViewById<TextView>(R.id.time).text = millisecondsToTime(it.duration)
            binding.timelineItems.addView(item)
            val params = item.layoutParams
            params.width = dpToPx(requireContext(), 80)
            item.layoutParams = params
        }
    }
}