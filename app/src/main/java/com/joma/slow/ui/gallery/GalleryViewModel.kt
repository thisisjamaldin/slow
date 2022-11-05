package com.joma.slow.ui.gallery

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joma.slow.model.MVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class GalleryViewModel : ViewModel() {

    var videos: MutableLiveData<MutableList<MVideo>> = MutableLiveData()

    fun getVideos(context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            val result: MutableList<MVideo> = ArrayList()
            val contentResolver: ContentResolver = context.contentResolver!!
            val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            val cursor = contentResolver.query(uri, null, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                    )
                    var vimage: Bitmap? = null
                    var duration = ""
                    try {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(context, contentUri)
                        duration =
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                ?: "0"
                        vimage = retriever.getFrameAtTime(0)
                        retriever.release()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val videoModel = MVideo(contentUri, vimage, duration)
                    result.add(videoModel)
                } while (cursor.moveToNext())
            }
            videos.postValue(result)
        }
    }
}