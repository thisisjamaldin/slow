package com.joma.slow.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import com.joma.slow.R
import com.joma.slow.databinding.FragmentMainBinding
import com.joma.slow.base.BaseFragment
import com.joma.slow.model.EType

class MainFragment: BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val bundle = Bundle()
                bundle.putString("uri", "${data?.data}")
                controller.navigate(R.id.cutFragment, bundle)
            }
        }

        binding.slowMoLayout.setOnClickListener {
            val bundle = bundleOf("type" to EType.SPEED)
            controller.navigate(R.id.galleryFragment, bundle)
        }

        binding.audioLayout.setOnClickListener {
            val bundle = bundleOf("type" to EType.AUDIO)
            controller.navigate(R.id.galleryFragment, bundle)
        }

        binding.bgLayout.setOnClickListener {
            val bundle = bundleOf("type" to EType.BACKGROUND)
            controller.navigate(R.id.galleryFragment, bundle)
        }

        binding.settings.setOnClickListener {
            controller.navigate(R.id.settingsFragment)
        }

    }

}