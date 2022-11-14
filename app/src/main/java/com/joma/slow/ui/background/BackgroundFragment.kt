package com.joma.slow.ui.background

import VideoHandle.OnEditorListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.joma.slow.R
import com.joma.slow.base.BaseFragment
import com.joma.slow.databinding.FragmentBackgroundBinding
import com.joma.slow.model.EType
import com.joma.slow.ui.utils.SimpleSeekBarListener
import com.joma.slow.ui.utils.millisecondsToTime
import java.io.File


class BackgroundFragment :
    BaseFragment<FragmentBackgroundBinding>(FragmentBackgroundBinding::inflate) {

    var progressHandler = Handler()
    lateinit var progressRunnable: Runnable
    lateinit var player: ExoPlayer
    var duration = 0L
    private val metrics = DisplayMetrics()
    var videoOriginHeight = 0
    var videoOriginWidth = 0
    var aspectWidth = 1
    var aspectHeight = 1

    lateinit var file: File


    val viewModel: BackgroundViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        file = File(
            requireContext().cacheDir.absolutePath + "/" + "project.mp4"
        )

        player = ExoPlayer.Builder(requireContext()).build()
        binding.video.player = player

        val mediaItem = MediaItem.fromUri(file.toUri())
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

        binding.video.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

        binding.back.setOnClickListener {
            player.stop()
            player.release()
            progressHandler.removeCallbacks(progressRunnable)
            controller.navigateUp()
        }


        duration = arguments?.getLong("duration") ?: 0
        binding.timelineText1.text = "00:00"
        binding.timelineText2.text = millisecondsToTime("${duration / 5}")
        binding.timelineText3.text = millisecondsToTime("${duration / 5 * 2}")
        binding.timelineText4.text = millisecondsToTime("${duration / 5 * 3}")
        binding.timelineText5.text = millisecondsToTime("${duration / 5 * 4}")
        binding.timelineText6.text = millisecondsToTime("$duration")
        val progressSeekbarListener = object : SimpleSeekBarListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val seek = progress.toFloat() / 200f * player.duration.toFloat()
                player.seekTo(seek.toLong())
            }
        }

        progressRunnable = Runnable {
            val progressBar = (player.currentPosition * 200 / player.duration).toInt()
            binding.progress.setOnSeekBarChangeListener(null)
            binding.progress.progress = progressBar
            binding.progress.setOnSeekBarChangeListener(progressSeekbarListener)
            binding.progressText.text =
                "${millisecondsToTime("${player.currentPosition}")}:${millisecondsToTime("${viewModel.duration.value}")}"

            progressHandler.postDelayed(progressRunnable, 10)
        }

        binding.red.setOnClickListener {
            binding.video.setBackgroundColor(resources.getColor(R.color.red))
        }


        viewModel.duration.observe(viewLifecycleOwner) {

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
        binding.progress.setOnSeekBarChangeListener(progressSeekbarListener)
        getPreview()

        binding.darkGrey.setOnClickListener {
            binding.video.setBackgroundColor(resources.getColor(R.color.blue_200))
        }

        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)

        binding.aspect11.setOnClickListener {
            aspectWidth = 1
            aspectHeight = 1
            setVideoAspect()
        }
        binding.aspect45.setOnClickListener {
            aspectWidth = 4
            aspectHeight = 5
            setVideoAspect()
        }
        binding.aspect169.setOnClickListener {
            aspectWidth = 16
            aspectHeight = 9
            setVideoAspect()
        }
        binding.aspect916.setOnClickListener {
            aspectWidth = 9
            aspectHeight = 16
            setVideoAspect()
        }
        viewModel.duration.observe(viewLifecycleOwner) {
            binding.progressText.text = "00:00-${millisecondsToTime("$it")}"
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                getViewPosition()
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun getViewPosition() {
        videoOriginHeight = binding.video.height
        videoOriginWidth = binding.video.width
    }

    private fun setVideoAspect() {
        val p = binding.video.layoutParams
        val width = videoOriginHeight / aspectHeight * aspectWidth
        val height = videoOriginWidth / aspectWidth * aspectHeight
        if (metrics.widthPixels < width) {
            p.height = height
            p.width = videoOriginWidth
        } else {
            p.width = width
            p.height = videoOriginHeight
        }
        binding.video.layoutParams = p
    }

    private fun getPreview() {
        viewModel.getPreviews(requireContext(), file.toUri())
        viewModel.previews.observe(viewLifecycleOwner) {
            binding.preview1.setImageBitmap(it[0])
            binding.preview2.setImageBitmap(it[1])
            binding.preview3.setImageBitmap(it[2])
            binding.preview4.setImageBitmap(it[3])
            binding.preview5.setImageBitmap(it[4])
        }
    }
}