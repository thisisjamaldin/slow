package com.joma.slow.ui.music

import VideoHandle.OnEditorListener
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.joma.slow.R
import com.joma.slow.base.BaseFragment
import com.joma.slow.databinding.FragmentMusicBinding
import com.joma.slow.model.MVideo
import com.joma.slow.ui.custom.LoadingDialog
import com.joma.slow.ui.custom.WarningDialog
import com.joma.slow.ui.utils.SimpleSeekBarListener
import com.joma.slow.ui.utils.dpToPx
import com.joma.slow.ui.utils.millisecondsToTime
import java.io.File

class MusicFragment : BaseFragment<FragmentMusicBinding>(FragmentMusicBinding::inflate),
    OnEditorListener {

    val viewModel: MusicViewModel by viewModels()
    var progressHandler = Handler()
    lateinit var progressRunnable: Runnable
    var timeLineList: MutableList<MVideo> = ArrayList()
    lateinit var player: ExoPlayer
    var scrolling = false
    private val metrics = DisplayMetrics()
    private var volumeBtnPosition = 0
    private var volume = 0.5f
    lateinit var mediaPlayer: MediaPlayer
    var backgroundAudio: Uri? = null
    var backgroundAudioPath = ""
    private val outFolder =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "slowMo")
    private val outFile = File(outFolder.absolutePath, "/${System.currentTimeMillis()}.mp4")
    private lateinit var bottomSheetBehaviorVolume: BottomSheetBehavior<ConstraintLayout>
    private lateinit var bottomSheetBehaviorExport: BottomSheetBehavior<ConstraintLayout>


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val file = File(
            requireContext().cacheDir.absolutePath + "/" + "project.mp4"
        )

        mediaPlayer = MediaPlayer()

        bottomSheetBehaviorVolume =
            BottomSheetBehavior.from(view.findViewById(R.id.bottom_sheet_volume_root))
        bottomSheetBehaviorExport =
            BottomSheetBehavior.from(view.findViewById(R.id.bottom_sheet_export_root))

        loadingDialog = LoadingDialog(requireContext())
        player = ExoPlayer.Builder(requireContext()).build()
        binding.video.player = player

        val mediaItem = MediaItem.fromUri(file.toUri())
        player.setMediaItem(mediaItem)
        player.prepare()
        player.volume = volume

        binding.export.setOnClickListener {
            bottomSheetBehaviorExport.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.volume.setOnClickListener {
            bottomSheetBehaviorVolume.state = BottomSheetBehavior.STATE_EXPANDED
        }

        progressRunnable = Runnable {
            val newPos = binding.timelineParent.scrollX.toFloat() - volumeBtnPosition
            binding.volume.animate()?.x(newPos * -1)?.setDuration(0)?.start()
            val progressBar = (player.currentPosition * (timeLineList.size * dpToPx(
                requireContext(),
                80
            )) / player.duration).toInt()
            if (!scrolling) {
                binding.timelineParent.scrollTo(progressBar, 0)
            }
            binding.playDuration.text =
                "${millisecondsToTime("${player.currentPosition}")}:${millisecondsToTime("${viewModel.duration.value}")}"
            progressHandler.postDelayed(progressRunnable, 10)
        }
        progressHandler.postDelayed(progressRunnable, 10)

        binding.back.setOnClickListener {
            WarningDialog(requireContext(), object : WarningDialog.Confirm {
                override fun tick() {
                    player.stop()
                    player.release()
                    mediaPlayer.stop()
                    progressHandler.removeCallbacks(progressRunnable)
                    controller.navigateUp()
                }
            })
        }

        view.findViewById<SeekBar>(R.id.slider_volume)
            .setOnSeekBarChangeListener(object : SimpleSeekBarListener() {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    super.onProgressChanged(seekBar, progress, fromUser)
                    player.volume = progress.toFloat() / 200
                    view.findViewById<TextView>(R.id.text_volume).text = "$progress"
                }
            })
        view.findViewById<ImageView>(R.id.tick).setOnClickListener {
            volume = player.volume
            bottomSheetBehaviorVolume.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        view.findViewById<ImageView>(R.id.close).setOnClickListener {
            player.volume = volume
            view.findViewById<SeekBar>(R.id.slider_volume).progress = (volume * 200).toInt()
            bottomSheetBehaviorVolume.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.play.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                mediaPlayer.pause()
                binding.play.setImageResource(R.drawable.bg_play_btn)
            } else {
                mediaPlayer.start()
                player.play()
                binding.play.setImageResource(R.drawable.bg_pause_btn)
            }
        }

        binding.addAudio.setOnClickListener {
            controller.navigate(R.id.pickMusicFragment)
            player.pause()
            mediaPlayer.pause()
        }

        setFragmentResultListener("key") { key, bundle ->
            player.seekTo(0)
            backgroundAudio = bundle.get("uri") as Uri
            backgroundAudioPath = bundle.getString("path") ?: ""
            mediaPlayer.setDataSource(requireContext(), backgroundAudio!!)
            mediaPlayer.prepare()
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
                    mediaPlayer.seekTo(seek.toInt())
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
            binding.playDuration.text = "00:00/${millisecondsToTime("$it")}"
        }

        viewModel.previews.observe(viewLifecycleOwner) { list ->
            timeLineList.clear()
            binding.timelineItems.removeAllViews()
            timeLineList.addAll(list)
            bindTimeLineItems()
        }
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    WarningDialog(requireContext(), object : WarningDialog.Confirm {
                        override fun tick() {
                            player.stop()
                            player.release()
                            mediaPlayer.stop()
                            progressHandler.removeCallbacks(progressRunnable)
                            controller.navigateUp()
                        }
                    })
                }
            })
        view.findViewById<TextView>(R.id.ultra_hd).setOnClickListener {
            if (backgroundAudio == null) {
                Toast.makeText(requireContext(), "Add audio first", Toast.LENGTH_LONG).show()
                bottomSheetBehaviorExport.state = BottomSheetBehavior.STATE_COLLAPSED
                return@setOnClickListener
            }
            if (!outFolder.exists()) {
                outFolder.mkdirs()
            }
            loadingDialog.showLoading()
            viewModel.addAudio(
                file.absolutePath,
                volume,
                backgroundAudioPath,
                outFile.absolutePath,
                this
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        progressHandler.removeCallbacks(progressRunnable)
        player.stop()
        mediaPlayer.stop()
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
        volumeBtnPosition = binding.timelineParent.scrollX + (metrics.widthPixels / 2) - dpToPx(
            requireContext(),
            50
        )
        binding.volume.visibility = View.VISIBLE
    }

    override fun onSuccess() {
        Handler(Looper.getMainLooper()).post {
            loadingDialog.hideLoading()
            controller.navigate(R.id.resultFragment, bundleOf("path" to outFile.absolutePath))
        }
    }

    override fun onFailure() {
        Handler(Looper.getMainLooper()).post {
            loadingDialog.hideLoading()
        }
    }

    override fun onProgress(progress: Float) {
        Handler(Looper.getMainLooper()).post {
            loadingDialog.progress((progress * 100).toInt())
        }
    }
}