package com.joma.slow.ui.cut

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.joma.slow.R
import com.joma.slow.databinding.FragmentCutBinding
import com.joma.slow.base.BaseFragment
import com.joma.slow.ui.utils.VideoUtils
import java.io.File


class CutFragment : BaseFragment<FragmentCutBinding>(FragmentCutBinding::inflate),
    VideoUtils.Listener {

    lateinit var file: File
    lateinit var uri: Uri
    var progressHandler = Handler()
    lateinit var progressRunnable: Runnable

    //slider
    var fromSliderXDir = 0f
    var toSliderXDir = 0f
    var fromSliderDestination = 0f
    var toSliderDestination = 0f
    var fromSliderOriginalPos = 0f
    var toSliderOriginalPos =0f
    var minSliderValue = 0f
    var maxSliderValue = 0f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val player = ExoPlayer.Builder(requireContext()).build()
        binding.video.player = player

        uri = Uri.parse(arguments?.getString("uri"))

        val progressSeekbarListener = object: OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val seek = progress.toFloat()/200f*player.duration.toFloat()
                player.seekTo(seek.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        }

        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.addListener(object : Player.Listener{
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying){
                    progressHandler.post(progressRunnable)
                } else {
                    progressHandler.removeCallbacks(progressRunnable)
                }
            }

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)){
                    binding.loading.visibility = View.VISIBLE
                }
                if (events.contains(Player.EVENT_RENDERED_FIRST_FRAME)){
                    binding.loading.visibility = View.GONE
                    binding.duration.text = "${player.duration}"
                }
//                for (a in 0 until events.size()){
//                    Log.e("-------${events[a]}", "1")
//                }
            }
        })

        progressRunnable = Runnable {
            val endSlider = toSliderDestination/(toSliderOriginalPos-(fromSliderOriginalPos + binding.cutFrom.width))*200
            val progressBar = (player.currentPosition*200/player.duration).toInt()
            if (progressBar >= endSlider){
                player.stop()
                progressHandler.removeCallbacks(progressRunnable)
                return@Runnable
            }
            binding.progress.setOnSeekBarChangeListener(null)
            binding.progress.progress = progressBar
            binding.progress.setOnSeekBarChangeListener(progressSeekbarListener)
            progressHandler.postDelayed(progressRunnable, 10)
        }

        file = File.createTempFile("video", ".mp4", requireContext().cacheDir)

        binding.video.keepScreenOn = true

        binding.play.setOnClickListener {
            if (player.isPlaying){
                player.pause()
                binding.play.setImageResource(R.drawable.ic_play)
            } else {
                player.play()
                binding.play.setImageResource(R.drawable.ic_pause)
            }
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(object: OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                getViewPos()
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        binding.cutFrom.setOnTouchListener(object: OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when(event?.action){
                    MotionEvent.ACTION_DOWN -> {
                        fromSliderXDir = v?.x?.minus(event.rawX) ?: 0f
                    }
                    MotionEvent.ACTION_MOVE -> {
                        fromSliderDestination = event.rawX + fromSliderXDir
                        if (fromSliderDestination > fromSliderOriginalPos && fromSliderDestination < maxSliderValue) {
                            v?.animate()?.x(fromSliderDestination)?.setDuration(0)?.start()
                            minSliderValue = fromSliderDestination
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        val progress = binding.cutFrom.x/(toSliderOriginalPos-(fromSliderOriginalPos + binding.cutFrom.width))
                        binding.progress.progress = (progress*200).toInt()
                        val seek = progress*player.duration.toFloat()
                        player.seekTo(seek.toLong())
                    }
                    else -> return false
                }
                return true
            }

        })

        binding.cutTo.setOnTouchListener(object: OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when(event?.action){
                    MotionEvent.ACTION_DOWN -> {
                        toSliderXDir = v?.x?.minus(event.rawX) ?: 0f
                    }
                    MotionEvent.ACTION_MOVE -> {
                        toSliderDestination = event.rawX + toSliderXDir
                        if (toSliderDestination < toSliderOriginalPos && toSliderDestination > minSliderValue) {
                            v?.animate()?.x(toSliderDestination)?.setDuration(0)?.start()
                            maxSliderValue = toSliderDestination
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        binding.red.x = binding.cutTo.x
                    }
                    else -> return false
                }
                return true
            }

        })

        binding.progress.setOnSeekBarChangeListener(progressSeekbarListener)

//        VideoUtils.startTrim(
//            requireContext(),
//            uri,
//            file.absolutePath,
//            0,
//            2000,
//            useAudio = true,
//            useVideo = true,
//            listener = this
//        )
        getPreview(player.duration*1000)
    }

    private fun getViewPos(){
        fromSliderOriginalPos = binding.cutFrom.x
        fromSliderDestination = binding.cutFrom.x

        toSliderOriginalPos = binding.cutTo.x
        toSliderDestination = toSliderOriginalPos

        maxSliderValue = toSliderOriginalPos
        minSliderValue = fromSliderOriginalPos
    }

    private fun getPreview(dur: Long){
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(requireContext(), uri)
            var interval: Long = 0
            //here 5 means frame at the 5th sec.
            val bitmap1 = retriever.getFrameAtTime(interval)
            binding.preview1.setImageBitmap(bitmap1)
            interval += dur / 5
            val bitmap2 = retriever.getFrameAtTime(interval)
            binding.preview2.setImageBitmap(bitmap2)
            interval += dur / 5
            val bitmap3 = retriever.getFrameAtTime(interval)
            binding.preview3.setImageBitmap(bitmap3)
            interval += dur / 5
            val bitmap4 = retriever.getFrameAtTime(interval)
            binding.preview4.setImageBitmap(bitmap4)
            interval += dur / 5
            val bitmap5 = retriever.getFrameAtTime(interval)
            binding.preview5.setImageBitmap(bitmap5)
        } catch (ex: Exception) {
            // Assume this is a corrupt video file
        }
    }

    override fun onProgress(value: Float) {
//        Toast.makeText(requireContext(), "$value", Toast.LENGTH_SHORT).show()
    }

    override fun onComplete() {
//        Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show()

//        binding.video.setVideoPath(file.absolutePath)
//        binding.video.start()
//        binding.video.requestFocus()
    }

    override fun onStart2() {
//        Toast.makeText(this, "Start!", Toast.LENGTH_SHORT).show()
    }

    override fun onError(message: String) {
//        Toast.makeText(this, "$message", Toast.LENGTH_SHORT).show()
    }
}