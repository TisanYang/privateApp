package com.tisan.share.base

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar
import com.kj.infinite.R

abstract class BaseActivity<VB : ViewBinding, VM : ViewModel> : AppCompatActivity() {

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


        // 设置状态栏透明 & 沉浸式
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        window.statusBarColor = Color.TRANSPARENT
//
//        // 设置状态栏字体为白色（即 isLight = false）
//        WindowCompat.getInsetsController(window, window.decorView).apply {
//            isAppearanceLightStatusBars = false // false = 白色字体
//        }
        _binding = inflateBinding()
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[viewModelClass]
        initViews()
        initData()
        observeData()
        initListeners()
    }

    private fun setStatusBar(darkFont: Boolean, bgColor: Int = Color.GREEN) {
        window.statusBarColor = bgColor
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = darkFont // true: 黑色字体, false: 白色字体
        }
    }


    private fun setTransparentStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
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
