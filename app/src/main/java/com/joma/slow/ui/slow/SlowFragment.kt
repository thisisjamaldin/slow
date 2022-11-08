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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.viewModels
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.joma.slow.R
import com.joma.slow.base.BaseFragment
import com.joma.slow.databinding.FragmentSlowBinding
import com.joma.slow.model.MSpeed
import com.joma.slow.model.MVideo
import com.joma.slow.ui.custom.LoadingDialog
import com.joma.slow.ui.utils.AdapterListener
import com.joma.slow.ui.utils.dpToPx
import com.joma.slow.ui.utils.millisecondsToTime


class SlowFragment : BaseFragment<FragmentSlowBinding>(FragmentSlowBinding::inflate) {

    val viewModel: SlowViewModel by viewModels()
    var progressHandler = Handler()
    lateinit var progressRunnable: Runnable
    var timeLineList: MutableList<MVideo> = ArrayList()
    lateinit var player: ExoPlayer
    var scrolling = false
    lateinit var loadingDialog: LoadingDialog
    var speeds: MutableList<MSpeed> = ArrayList()
    private val metrics = DisplayMetrics()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uri = Uri.parse(arguments?.getString("path"))

//        val file = File(
//            Environment.getExternalStorageDirectory().toString() + "/" + "project.mp4"
//        )

        loadingDialog = LoadingDialog(requireContext())
        player = ExoPlayer.Builder(requireContext()).build()
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

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) {
                    binding.loading.visibility = View.VISIBLE
                }
                if (events.contains(Player.EVENT_RENDERED_FIRST_FRAME)) {
                    binding.loading.visibility = View.GONE
                }
            }
        })

        progressRunnable = Runnable {
            moveSpeeds()
            val progressBar = (player.currentPosition * (timeLineList.size * dpToPx(
                requireContext(),
                80
            )) / player.duration).toInt()
            if (!scrolling) {
                binding.timelineParent.scrollTo(progressBar, 0)
            }
            for (speed in speeds){
                if (speed.positionOnTimeLine < binding.timelineParent.scrollX + (metrics.widthPixels / 2) && speed.positionOnTimeLine+speed.width > binding.timelineParent.scrollX + (metrics.widthPixels / 2)){
                    if (speed.speedUp) {
                        player.setPlaybackSpeed(0.5f)
                    } else {
                        player.setPlaybackSpeed(2f)
                    }
                } else {
                    player.setPlaybackSpeed(1f)

                }
            }
            Log.e("-------21324", "${binding.timelineParent.scrollX + (metrics.widthPixels / 2)}")
            progressHandler.postDelayed(progressRunnable, 10)
        }

        binding.back.setOnClickListener {
            player.stop()
            player.release()
            progressHandler.removeCallbacks(progressRunnable)
            controller.navigateUp()
        }

        binding.play.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                binding.play.setImageResource(R.drawable.bg_play_btn)
            } else {
                player.prepare()
                player.play()
                binding.play.setImageResource(R.drawable.bg_pause_btn)
            }
        }

        binding.timelineParent.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
//                    currentTimeLinePosition = binding.timelineParent.scrollX.toFloat()
                    scrolling = true
                }
                MotionEvent.ACTION_UP -> {
                    val seek =
                        binding.timelineParent.scrollX.toFloat() / (timeLineList.size.toFloat() * dpToPx(
                            requireContext(),
                            80
                        ).toFloat()) * player.duration.toFloat()
                    player.seekTo(seek.toLong())
                    scrolling = false
                    moveSpeeds()
                }
                MotionEvent.ACTION_MOVE -> {
                    moveSpeeds()
                }
            }
            false
        }

        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
        binding.timelineParent.setPadding(
            (metrics.widthPixels - dpToPx(requireContext(), 48)) / 2,
            0,
            (metrics.widthPixels - dpToPx(requireContext(), 48)) / 2,
            0
        )
        viewModel.getPreviews(requireContext(), uri)

        viewModel.duration.observe(viewLifecycleOwner) {
            binding.currentDuration.text = millisecondsToTime("$it")
        }

        viewModel.previews.observe(viewLifecycleOwner) { list ->
            timeLineList.clear()
            timeLineList.addAll(list)
            bindTimeLineItems()
        }

        binding.speedUp.setOnClickListener {
            addSpeed(true)
        }
        binding.speedDown.setOnClickListener {
            addSpeed(false)
        }
    }

    override fun onDetach() {
        super.onDetach()
        progressHandler.removeCallbacks(progressRunnable)
        player.stop()
    }

    private fun addSpeed(speedUp: Boolean) {
        val item = layoutInflater.inflate(R.layout.item_time_line_speed, null)
        val res = requireContext().resources
        item.findViewById<View>(R.id.background).setBackgroundColor(
            if (speedUp) res.getColor(R.color.transparent_red) else res.getColor(R.color.transparent_blue)
        )
        item.findViewById<View>(R.id.speed_text).setBackgroundResource(
            if (speedUp) R.drawable.bg_rounded_red else R.drawable.bg_rounded_blue
        )
        binding.root.addView(item)
        val params = item.layoutParams as ConstraintLayout.LayoutParams
        params.startToStart = binding.timelineParent.id
        params.endToEnd = binding.timelineParent.id
        params.bottomToTop = binding.timelineParent.id
        params.marginStart = dpToPx(requireContext(), 94)
        params.bottomMargin = dpToPx(requireContext(), -50)
        params.width = dpToPx(requireContext(), 100)
        item.layoutParams = params
        speeds.add(
            MSpeed(
                item,
                dpToPx(requireContext(), 100),
                binding.timelineParent.scrollX + (metrics.widthPixels / 2),
                speedUp
            )
        )
        item.setOnLongClickListener {
            binding.root.removeView(item)
            true
        }
    }

    private fun moveSpeeds() {
        for (speed in speeds) {
            val newPos = binding.timelineParent.scrollX.toFloat() - speed.positionOnTimeLine
            speed.view.animate()?.x(newPos * -1)?.setDuration(0)?.start()
        }
    }

    private fun bindTimeLineItems() {
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