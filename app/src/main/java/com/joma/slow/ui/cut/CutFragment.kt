package com.joma.slow.ui.cut

import VideoHandle.OnEditorListener
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.joma.slow.R
import com.joma.slow.base.BaseFragment
import com.joma.slow.databinding.FragmentCutBinding
import com.joma.slow.model.EType
import com.joma.slow.ui.custom.LoadingDialog
import com.joma.slow.ui.utils.SimpleSeekBarListener
import com.joma.slow.ui.utils.VideoUtils
import com.joma.slow.ui.utils.millisecondsToTime
import java.io.File
import java.lang.Exception


class CutFragment : BaseFragment<FragmentCutBinding>(FragmentCutBinding::inflate),
    VideoUtils.Listener {

    lateinit var uri: Uri
    lateinit var type: EType
    var duration = 0L
    var progressHandler = Handler()
    lateinit var progressRunnable: Runnable
    lateinit var player: ExoPlayer

    val viewModel: CutViewModel by viewModels()

    //slider
    var fromSliderXDir = 0f
    var toSliderXDir = 0f
    var fromSliderDestination = 0f
    var toSliderDestination = 0f
    var fromSliderOriginalPos = 0f
    var toSliderOriginalPos = 0f
    var minSliderValue = 0f
    var maxSliderValue = 0f


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uri = Uri.parse(arguments?.getString("uri"))
        type = arguments?.get("type") as EType

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
            progressHandler.postDelayed(progressRunnable, 10)
        }

//        file = File.createTempFile("video", ".mp4", requireContext().cacheDir)

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

        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                getViewPosition()
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        binding.cutFrom.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        fromSliderXDir = v?.x?.minus(event.rawX) ?: 0f
                    }
                    MotionEvent.ACTION_MOVE -> {
                        fromSliderDestination = event.rawX + fromSliderXDir
                        if (fromSliderDestination > fromSliderOriginalPos && fromSliderDestination < maxSliderValue - 100) {
                            v!!.animate()?.x(fromSliderDestination)?.setDuration(0)?.start()
                            changeTimeLineLines()
                            minSliderValue = fromSliderDestination
                        }
                    }
                    else -> return false
                }
                return true
            }

        })

        binding.cutTo.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        toSliderXDir = v?.x?.minus(event.rawX) ?: 0f
                    }
                    MotionEvent.ACTION_MOVE -> {
                        toSliderDestination = event.rawX + toSliderXDir
                        if (toSliderDestination < toSliderOriginalPos && toSliderDestination > minSliderValue + 100) {
                            v?.animate()?.x(toSliderDestination)?.setDuration(0)?.start()
                            changeTimeLineLines()
                            maxSliderValue = toSliderDestination
                        }
                    }
                    else -> return false
                }
                return true
            }

        })


        binding.progress.setOnSeekBarChangeListener(progressSeekbarListener)
        binding.next.setOnClickListener {
            var end =
                (binding.cutTo.x - binding.cutTo.width) / (toSliderOriginalPos - (fromSliderOriginalPos + binding.cutFrom.width)) * 200
            var start =
                binding.cutFrom.x / (toSliderOriginalPos - (fromSliderOriginalPos + binding.cutFrom.width)) * 200
            loadingDialog.showLoading()
            if (binding.cutFrom.x == fromSliderOriginalPos) {
                start = -1f
            }
            if (binding.cutTo.x == toSliderOriginalPos) {
                end = -1f
            }
            viewModel.cut(
                requireContext(),
                uri,
                duration / 200 * start,
                (duration / 200 * end),
                this
            )
        }

        getPreview(duration * 1000)
    }

    private fun changeTimeLineLines() {
        val params = binding.timelineTopLine.layoutParams
        val params2 = binding.timelineBottomLine.layoutParams
        binding.timelineTopLine.x = binding.cutFrom.x + binding.cutFrom.width / 2
        binding.timelineBottomLine.x = binding.cutFrom.x + binding.cutFrom.width / 2

        params.width = (binding.cutTo.x - fromSliderDestination).toInt()
        params2.width = (binding.cutTo.x - fromSliderDestination).toInt()

        binding.timelineTopLine.layoutParams = params
        binding.timelineBottomLine.layoutParams = params2
    }

    private fun getViewPosition() {
        fromSliderOriginalPos = binding.cutFrom.x
        fromSliderDestination = binding.cutFrom.x

        toSliderOriginalPos = binding.cutTo.x
        toSliderDestination = toSliderOriginalPos

        maxSliderValue = toSliderOriginalPos
        minSliderValue = fromSliderOriginalPos
    }

    private fun getPreview(dur: Long) {
        viewModel.getPreviews(requireContext(), uri, dur)
        viewModel.previews.observe(viewLifecycleOwner) {
            binding.preview1.setImageBitmap(it[0])
            binding.preview2.setImageBitmap(it[1])
            binding.preview3.setImageBitmap(it[2])
            binding.preview4.setImageBitmap(it[3])
            binding.preview5.setImageBitmap(it[4])
        }
    }

    override fun onProgress(value: Float) {
        Log.e("--------234234", "$value")
        loadingDialog.progress((value * 100).toInt())
    }

    override fun onComplete(path: String) {
        val bundle = Bundle()
        bundle.putString("path", "$path")
        when (type) {
            EType.SPEED ->
                controller.navigate(R.id.slowFragment, bundle)
            EType.AUDIO ->
                controller.navigate(R.id.musicFragment, bundle)
            EType.CURVE ->
                controller.navigate(R.id.musicFragment, bundle)
            EType.BACKGROUND ->
                controller.navigate(R.id.backgroundFragment, bundle)
        }
        loadingDialog.hideLoading()
        player.stop()
        player.release()
    }

    override fun onStart2() {

    }

    override fun onError(message: String) {
        loadingDialog.hideLoading()
        Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
        player.stop()
        player.release()
    }

    override fun onDetach() {
        super.onDetach()
        progressHandler.removeCallbacks(progressRunnable)
        player.stop()
        player.release()
        loadingDialog.hideLoading()
    }

}