package com.tisan.share.base

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding,VM : ViewModel> : AppCompatActivity() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    // 子类提供对应的 ViewModel class
    lateinit var viewModel: VM
    protected abstract val viewModelClass: Class<VM>

    // 由子类实现，提供ViewBinding的inflate方法
    abstract fun inflateBinding(): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransparentStatusBar()
        _binding = inflateBinding()
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[viewModelClass]
        initViews()
        initData()
        observeData()
        initListeners()
    }

    private fun setTransparentStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = Color.TRANSPARENT
    }
    protected open fun initViews() {}
    protected open fun initData() {}
    protected open fun observeData() {}
    protected open fun initListeners() {}

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
