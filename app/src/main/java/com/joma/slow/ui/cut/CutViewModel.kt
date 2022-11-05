package com.joma.slow.ui.cut

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joma.slow.ui.utils.VideoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class CutViewModel: ViewModel() {

    var previews: MutableLiveData<MutableList<Bitmap?>> = MutableLiveData()

    fun getPreviews(context: Context, uri: Uri, dur: Long){
        viewModelScope.launch(Dispatchers.Default) {
            val result: MutableList<Bitmap?> = ArrayList()
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
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

    fun cut(context: Context, uri: Uri, start: Float, end: Float, listener: VideoUtils.Listener){
//        viewModelScope.launch(Dispatchers.Default) {
        val f = File(context.cacheDir.absolutePath + "/" + "project0.mp4")
            f.createNewFile()
            val in_file = context.contentResolver.openInputStream(uri)!!
            val out = FileOutputStream(f)
            val buf = ByteArray(1024)
            var len: Int
            while (in_file.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            out.close()
            in_file.close()

        val file = File(
            context.cacheDir.absolutePath + "/" + "project.mp4"
        )
        file.createNewFile()

        VideoUtils.startTrim(
            f.absolutePath,
            file.absolutePath,
            start.toInt(),
            end.toInt(),
            useAudio = true,
            useVideo = true,
            listener = listener)

//        }


    }
}