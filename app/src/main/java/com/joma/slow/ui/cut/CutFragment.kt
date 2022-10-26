package com.joma.slow.ui.cut

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.MediaController
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.joma.slow.databinding.FragmentCutBinding
import com.joma.slow.ui.base.BaseFragment
import com.joma.slow.ui.utils.VideoUtils
import java.io.File


class CutFragment : BaseFragment<FragmentCutBinding>(FragmentCutBinding::inflate),
    VideoUtils.Listener {

    lateinit var file: File
    lateinit var previewFile: File

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uri = Uri.parse(arguments?.getString("uri"))

        file = File.createTempFile("video", ".mp4", requireContext().cacheDir)
        previewFile = File.createTempFile("videoP", ".mp4", requireContext().cacheDir)

        val mp: MediaPlayer = MediaPlayer.create(requireContext(), uri)
        val duration = mp.duration
        mp.release()
        binding.duration.text = "$duration"

        VideoUtils.startTrim(
            requireContext(),
            uri,
            file.absolutePath,
            0,
            2000,
            useAudio = true,
            useVideo = true,
            listener = this
        )

        var j = 0
        for (i in 0..duration step duration/5){
            Log.e("--------$i", "ab")
            VideoUtils.startTrim(
                requireContext(),
                uri,
                previewFile.absolutePath,
                i,
                duration,
                useAudio = true,
                useVideo = true,
                listener = object: VideoUtils.Listener{
                    override fun onStart2() {

                    }

                    override fun onProgress(value: Float) {

                    }

                    override fun onComplete() {
                        when(j){
                            0->{
                                Glide.with(requireContext()).load(previewFile.absolutePath).into(binding.preview1)
                            }
                            1->{
                                Glide.with(requireContext()).load(previewFile.absolutePath).into(binding.preview2)
                            }
                            2->{
                                Glide.with(requireContext()).load(previewFile.absolutePath).into(binding.preview3)
                            }
                            3->{
                                Glide.with(requireContext()).load(previewFile.absolutePath).into(binding.preview4)
                            }
                            4->{
                                Glide.with(requireContext()).load(previewFile.absolutePath).into(binding.preview5)
                            }
                        }
                        j++
                    }

                    override fun onError(message: String) {

                    }

                }
            )
        }

    }

    private fun getPreview(){

    }

    override fun onProgress(value: Float) {
//        Toast.makeText(requireContext(), "$value", Toast.LENGTH_SHORT).show()
    }

    override fun onComplete() {
//        Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show()
        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(binding.video)
        binding.video.setMediaController(mediaController)
        binding.video.keepScreenOn = true
        binding.video.setVideoPath(file.absolutePath)
        binding.video.start()
        binding.video.requestFocus()
    }

    override fun onStart2() {
//        Toast.makeText(this, "Start!", Toast.LENGTH_SHORT).show()
    }

    override fun onError(message: String) {
//        Toast.makeText(this, "$message", Toast.LENGTH_SHORT).show()
    }
}