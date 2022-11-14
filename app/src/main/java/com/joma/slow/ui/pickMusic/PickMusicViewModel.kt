package com.joma.slow.ui.pickMusic

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joma.slow.model.MAudio
import com.joma.slow.model.MVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class PickMusicViewModel : ViewModel(){

    var musics: MutableLiveData<MutableList<MAudio>> = MutableLiveData()

    fun getMusics(context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            val result: MutableList<MAudio> = ArrayList()
            val contentResolver: ContentResolver = context.contentResolver!!
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

            val selection = StringBuilder("is_music != 0 AND title != ''")
            val cursor = contentResolver.query(uri, null, selection.toString(), null, sortOrder)

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val title: String =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                    val duration: Int =
                        cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    )
                    val size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                    val path =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    result.add(MAudio(contentUri, size, title,"$duration", path))

                } while (cursor.moveToNext())
            }
            cursor?.close()
            musics.postValue(result)
        }
    }
}