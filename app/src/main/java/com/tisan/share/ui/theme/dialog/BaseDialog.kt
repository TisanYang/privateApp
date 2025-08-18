package com.tisan.share.ui.theme.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.kj.infinite.R

abstract class BaseDialog<VB : ViewBinding>(
    context: Context,
    @LayoutRes private val layoutId: Int,
    private val bindingInflater: (LayoutInflater) -> VB
) : Dialog(context, R.style.BaseDialogTheme) {

    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindingInflater(layoutInflater)
        setContentView(binding.root)

        // 设置宽度、位置、动画等
        window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (context.resources.displayMetrics.widthPixels * 0.85).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
        }

        setCancelable(true)
        setCanceledOnTouchOutside(false)

        initView()
    }

    protected abstract fun initView()
}
