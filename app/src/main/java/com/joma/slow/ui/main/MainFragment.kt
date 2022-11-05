package com.joma.slow.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.joma.slow.R
import com.joma.slow.databinding.FragmentMainBinding
import com.joma.slow.base.BaseFragment

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
            controller.navigate(R.id.galleryFragment)
        }

        binding.settings.setOnClickListener {
            controller.navigate(R.id.settingsFragment)
        }

    }

}