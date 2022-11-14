package com.joma.slow.ui.slow

import VideoHandle.EpEditor
import VideoHandle.OnEditorListener
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.appcompat.widget.ViewUtils
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joma.slow.model.MVideo
import com.joma.slow.ui.utils.VideoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class SlowViewModel : ViewModel() {

    var previews: MutableLiveData<MutableList<MVideo>> = MutableLiveData()
    var duration: MutableLiveData<Int> = MutableLiveData()

    fun getPreviews(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.Default) {
            val result: MutableList<MVideo> = ArrayList()
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                var dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                    .toInt()
                duration.postValue(dur)
                dur *= 1000
                var interval: Long = 0
                for (i in 0..dur step 4000000) {
                    val bitmap = retriever.getFrameAtTime(interval)
                    result.add(MVideo(null, bitmap, "${interval / 1000}"))
                    interval += dur / (dur / 4000000)
                }
            } catch (ex: Exception) {
            }
            previews.postValue(result)
        }
    }

    fun export(context: Context, pathMain: String) {

    }

}