package com.tisan.share.base

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<Binding : ViewBinding, VM : ViewModel> : Fragment() {

    lateinit var binding: Binding
    lateinit var viewModel: VM

    // 子类提供对应的 ViewModel class
    protected abstract val viewModelClass: Class<VM>

    // 子类提供 Binding inflate 方法
    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        makeStatusBarTransparent()
        binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[viewModelClass]
        initView()
        initData()
        observeData()
        initListeners()
    }

    open fun initView() {}
    open fun initData(){}
    open fun observeData() {}
    open fun initListeners(){}

    protected fun log(msg: String) {
        Log.d(this::class.java.simpleName, msg)
    }

    protected fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }


    private fun makeStatusBarTransparent() {
        activity?.let {
            WindowCompat.setDecorFitsSystemWindows(it.window, false)
            WindowInsetsControllerCompat(it.window, it.window.decorView).isAppearanceLightStatusBars = true
            it.window.statusBarColor = Color.TRANSPARENT
        }
    }
}
