package com.joma.slow.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.joma.slow.R
import com.joma.slow.ui.custom.LoadingDialog


typealias Inflate<B> = (LayoutInflater, ViewGroup?, Boolean) -> B

abstract class BaseFragment<B : ViewBinding>(val inflate: Inflate<B>) :
    Fragment() {

    lateinit var controller: NavController
    private var _binding: B? = null
    val binding get() = _binding!!
    lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val nav =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        controller = nav.navController
        _binding = inflate.invoke(layoutInflater, container, false)
        loadingDialog = LoadingDialog(requireContext())
//        controller = Navigation.findNavController(requireActivity(), R.id.nav_host)
        return binding.root
    }

}