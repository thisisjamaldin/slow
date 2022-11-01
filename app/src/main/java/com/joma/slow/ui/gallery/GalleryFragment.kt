package com.joma.slow.ui.gallery

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import com.joma.slow.base.BaseFragment
import com.joma.slow.databinding.FragmentGalleryBinding
import com.joma.slow.ui.utils.AdapterListener
import java.io.File

class GalleryFragment: BaseFragment<FragmentGalleryBinding>(FragmentGalleryBinding::inflate),
    AdapterListener {

    lateinit var adapter: GalleryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = GalleryAdapter(this)
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter.setList(getAllMedia())
    }

    fun getAllMedia(): ArrayList<Uri> {
        val videoItemHashSet: HashSet<Uri> = HashSet()
        val projection = arrayOf(
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.Media.DISPLAY_NAME
        )
        val cursor: Cursor? = requireActivity().contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        try {
            cursor?.moveToFirst()
            var count = 100000
            do {
                cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    ?.let {
                        val file = File.createTempFile("$count", ".mp4", requireContext().cacheDir)
                        videoItemHashSet.add(file.toUri())
                        count++
                    }
            } while (cursor?.moveToNext() == true)
            cursor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ArrayList(videoItemHashSet)
    }

    override fun click(pos: Int) {

    }
}