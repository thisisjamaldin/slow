package com.joma.slow.ui.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.joma.slow.R
import com.joma.slow.base.BaseFragment
import com.joma.slow.databinding.FragmentGalleryBinding
import com.joma.slow.ui.utils.AdapterListener


class GalleryFragment : BaseFragment<FragmentGalleryBinding>(FragmentGalleryBinding::inflate),
    AdapterListener {

    private lateinit var adapter: GalleryAdapter
    val viewModel: GalleryViewModel by viewModels()
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = GalleryAdapter(this)
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        viewModel.videos.observe(viewLifecycleOwner) {
            adapter.setList(it)
            binding.loading.visibility = View.GONE
        }
        binding.back.setOnClickListener {
            controller.navigateUp()
        }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.getVideos(requireContext())
            } else {
                controller.navigateUp()
            }
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                viewModel.getVideos(requireContext())
            }
        } else {
            viewModel.getVideos(requireContext())
        }
    }

    override fun click(pos: Int) {
        val bundle = Bundle()
        bundle.putString("uri", "${adapter.getVideo(pos).uri}")
        bundle.putLong("duration", adapter.getVideo(pos).duration.toLongOrNull()?:0)
        controller.navigate(R.id.cutFragment, bundle)
    }
}