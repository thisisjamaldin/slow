package com.joma.slow.ui.slow

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.joma.slow.R
import com.joma.slow.base.BaseFragment
import com.joma.slow.databinding.FragmentSlowBinding
import com.joma.slow.model.MSpeed
import com.joma.slow.model.MVideo
import com.joma.slow.ui.custom.HowItWorksDialog
import com.joma.slow.ui.custom.LoadingDialog
import com.joma.slow.ui.custom.WarningDialog
import com.joma.slow.ui.utils.dpToPx
import com.joma.slow.ui.utils.millisecondsToTime
import java.io.File

class SlowFragment : BaseFragment<FragmentSlowBinding>(FragmentSlowBinding::inflate) {

    val viewModel: SlowViewModel by viewModels()
    var progressHandler = Handler()
    lateinit var progressRunnable: Runnable
    var timeLineList: MutableList<MVideo> = ArrayList()
    lateinit var player: ExoPlayer
    var scrolling = false
    var speeds: MutableList<MSpeed> = ArrayList()
    private val metrics = DisplayMetrics()
    var paths: MutableList<String> = ArrayList()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val uri = Uri.parse(arguments?.getString("path"))
        val file = File(
            requireContext().cacheDir.absolutePath + "/" + "project.mp4"
        )
        bottomSheetBehavior =
            BottomSheetBehavior.from(view.findViewById(R.id.bottom_sheet_export_root))

        loadingDialog = LoadingDialog(requireContext())
        player = ExoPlayer.Builder(requireContext()).build()
        binding.video.player = player

        val mediaItem = MediaItem.fromUri(file.toUri())
        player.setMediaItem(mediaItem)
        player.prepare()

        binding.export.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        view.findViewById<TextView>(R.id.ultra_hd).setOnClickListener {
            viewModel.export(requireContext(), file.absolutePath)
        }
        view.findViewById<TextView>(R.id.full_hd).setOnClickListener {
            viewModel.export(requireContext(), file.absolutePath)
        }
        view.findViewById<TextView>(R.id.hd).setOnClickListener {
            viewModel.export(requireContext(), file.absolutePath)
        }

        progressRunnable = Runnable {
            moveSpeeds()
            val progressBar = (player.currentPosition * (timeLineList.size * dpToPx(
                requireContext(),
                80
            )) / player.duration).toInt()
            if (!scrolling) {
                binding.timelineParent.scrollTo(progressBar, 0)
            }
            for (speed in speeds) {
                if (speed.positionOnTimeLine < binding.timelineParent.scrollX + (metrics.widthPixels / 2) && speed.positionOnTimeLine + speed.width > binding.timelineParent.scrollX + (metrics.widthPixels / 2)) {
                    player.setPlaybackSpeed(speed.speed)
                } else {
                    player.setPlaybackSpeed(1f)
                }
            }
            progressHandler.postDelayed(progressRunnable, 10)
        }
        progressHandler.postDelayed(progressRunnable, 10)

        binding.back.setOnClickListener {
            WarningDialog(requireContext(), object : WarningDialog.Confirm {
                override fun tick() {
                    player.stop()
                    player.release()
                    progressHandler.removeCallbacks(progressRunnable)
                    controller.navigateUp()
                }
            })
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

        binding.howItWorks.setOnClickListener {
            HowItWorksDialog(requireContext())
        }

        binding.timelineParent.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
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
        viewModel.getPreviews(requireContext(), file.toUri())

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
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    WarningDialog(requireContext(), object : WarningDialog.Confirm {
                        override fun tick() {
                            player.stop()
                            player.release()
                            progressHandler.removeCallbacks(progressRunnable)
                            controller.navigateUp()
                        }
                    })
                }
            })
    }

    override fun onDetach() {
        super.onDetach()
        progressHandler.removeCallbacks(progressRunnable)
        player.stop()
    }

    private fun addSpeed(speedUp: Boolean) {
        if (speeds.size >= 5) {
            Toast.makeText(requireContext(), "Max 5", Toast.LENGTH_SHORT).show()
            return
        }
        val item = layoutInflater.inflate(R.layout.item_time_line_speed, null)
        val bg = item.findViewById<View>(R.id.background)
        val startLine = item.findViewById<View>(R.id.item_start)
        val endLine = item.findViewById<View>(R.id.item_end)
        val res = requireContext().resources
        bg.setBackgroundColor(
            if (speedUp) res.getColor(R.color.transparent_red) else res.getColor(R.color.transparent_blue)
        )
        item.findViewById<View>(R.id.speed_text).setBackgroundResource(
            if (speedUp) R.drawable.bg_rounded_red else R.drawable.bg_rounded_blue
        )
        binding.mainLayout.addView(item)
        val params = item.layoutParams as ConstraintLayout.LayoutParams
        params.startToStart = binding.timelineParent.id
        params.endToEnd = binding.timelineParent.id
        params.bottomToTop = binding.timelineParent.id
        params.marginStart = dpToPx(requireContext(), 94)
        params.bottomMargin = dpToPx(requireContext(), -50)
        params.width = dpToPx(requireContext(), 80)
        item.layoutParams = params
        val mSpeed = MSpeed(
            item,
            dpToPx(requireContext(), 80),
            binding.timelineParent.scrollX + (metrics.widthPixels / 2),
            if (speedUp) 2f else 0.5f
        )
        speeds.add(mSpeed)
        item.setOnLongClickListener {
            binding.mainLayout.removeView(item)
            speeds.remove(mSpeed)
            getNewDuration()
            true
        }
        getNewDuration()
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

    private fun getNewDuration() {
        var newDur = viewModel.duration.value!!
        for (speed in speeds) {
            if (speed.speed < 1) {
                newDur += newDur / viewModel.previews.value?.size!! / 2
            } else {
                newDur -= newDur / viewModel.previews.value?.size!! / 2
            }
        }
        binding.endDuration.text = millisecondsToTime("$newDur")
    }


}