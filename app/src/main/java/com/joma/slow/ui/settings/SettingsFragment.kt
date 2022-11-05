package com.joma.slow.ui.settings

import android.os.Bundle
import android.view.View
import com.joma.slow.BuildConfig
import com.joma.slow.base.BaseFragment
import com.joma.slow.databinding.FragmentSettingsBinding

class SettingsFragment: BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.version.text = "v ${BuildConfig.VERSION_NAME}"
    }
}