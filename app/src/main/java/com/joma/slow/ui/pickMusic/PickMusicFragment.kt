package com.joma.slow.ui.pickMusic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.joma.slow.base.BaseFragment
import com.joma.slow.databinding.FragmentPickMusicBinding
import com.joma.slow.ui.utils.AdapterListener

class PickMusicFragment : BaseFragment<FragmentPickMusicBinding>(FragmentPickMusicBinding::inflate),
    AdapterListener {

    private lateinit var adapter: PickMusicAdapter
    val viewModel: PickMusicViewModel by viewModels()
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = PickMusicAdapter(this)
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        viewModel.musics.observe(viewLifecycleOwner) {
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
                viewModel.getMusics(requireContext())
            } else {
                controller.navigateUp()
            }
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                viewModel.getMusics(requireContext())
            }
        } else {
            viewModel.getMusics(requireContext())
        }
    }

    override fun click(pos: Int) {
        setFragmentResult("key", bundleOf("path" to adapter.get(pos).path, "uri" to adapter.get(pos).uri))
        controller.navigateUp()
    }
}