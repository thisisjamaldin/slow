package com.joma.slow.ui.slow

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joma.slow.model.MVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SlowViewModel: ViewModel() {

    var previews: MutableLiveData<MutableList<MVideo>> = MutableLiveData()

    fun getPreviews(context: Context, uri: Uri){
        viewModelScope.launch(Dispatchers.Default) {
            val result: MutableList<MVideo> = ArrayList()
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt()*1000
                var interval: Long = 0
                var bitmap = retriever.getFrameAtTime(interval)
                result.add(MVideo(null, bitmap, "${interval/1000}"))
                interval += dur / 5
                bitmap = retriever.getFrameAtTime(interval)
                result.add(MVideo(null, bitmap, "${interval/1000}"))
                interval += dur / 5
                bitmap = retriever.getFrameAtTime(interval)
                result.add(MVideo(null, bitmap, "${interval/1000}"))
                interval += dur / 5
                bitmap = retriever.getFrameAtTime(interval)
                result.add(MVideo(null, bitmap, "${interval/1000}"))
                interval += dur / 5
                bitmap = retriever.getFrameAtTime(interval)
                result.add(MVideo(null, bitmap, "${interval/1000}"))
            } catch (ex: Exception) {
                // Assume this is a corrupt video file
            }
            previews.postValue(result)
        }
    }


}