package com.joma.slow.ui.gallery

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.joma.slow.base.BaseFragment
import com.joma.slow.databinding.FragmentGalleryBinding
import com.joma.slow.model.MVideo
import com.joma.slow.ui.utils.AdapterListener
import java.io.IOException


class GalleryFragment : BaseFragment<FragmentGalleryBinding>(FragmentGalleryBinding::inflate),
    AdapterListener {

    private lateinit var adapter: GalleryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = GalleryAdapter(this)
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter.setList(getAllMedia())
        binding.back.setOnClickListener {
            controller.navigateUp()
        }
    }

    fun getAllMedia(): List<MVideo> {
        val videoItems: MutableList<MVideo> = ArrayList()
        val contentResolver: ContentResolver = context?.contentResolver!!
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
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(requireContext(), contentUri)
                    duration =
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                    vimage = retriever.getFrameAtTime(0)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                val videoModel = MVideo(contentUri, vimage, duration)
                videoItems.add(videoModel)
            } while (cursor.moveToNext())
        }
        return videoItems
    }

    override fun click(pos: Int) {

    }
}