package com.joma.slow.ui.background

import VideoHandle.EpEditor
import VideoHandle.EpVideo
import VideoHandle.OnEditorListener
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class BackgroundViewModel: ViewModel() {

    var duration: MutableLiveData<Int> = MutableLiveData()
    var previews: MutableLiveData<MutableList<Bitmap?>> = MutableLiveData()

    fun getPreviews(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.Default) {
            val result: MutableList<Bitmap?> = ArrayList()
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                var dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt()
                duration.postValue(dur)
                dur *= 1000
                var interval: Long = 0
                var bitmap = retriever.getFrameAtTime(interval)
                result.add(bitmap)
                interval += dur / 5
                bitmap = retriever.getFrameAtTime(interval)
                result.add(bitmap)
                interval += dur / 5
                bitmap = retriever.getFrameAtTime(interval)
                result.add(bitmap)
                interval += dur / 5
                bitmap = retriever.getFrameAtTime(interval)
                result.add(bitmap)
                interval += dur / 5
                bitmap = retriever.getFrameAtTime(interval)
                result.add(bitmap)
            } catch (ex: Exception) {
                // Assume this is a corrupt video file
            }
            previews.postValue(result)
        }
    }
}