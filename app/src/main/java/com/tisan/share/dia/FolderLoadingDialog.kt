package com.tisan.share.dia

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.WindowManager
import com.kj.infinite.R
import com.kj.infinite.databinding.DialogFolderLoadingBinding
import com.tisan.share.ui.theme.dialog.BaseDialog

class FolderLoadingDialog(context: Context) :
    BaseDialog<DialogFolderLoadingBinding>(
        context,
        R.layout.dialog_folder_loading,
        DialogFolderLoadingBinding::inflate
    ) {

    override fun initView() {
        startAnim()

        // 👇 加这一段代码来设置你想要的宽度，比如 100dp
        setDialogWidth(150)
    }

    override fun show() {
        super.show()
        startAnim()
    }

    override fun dismiss() {
        stopAnim()
        super.dismiss()
    }

    fun setText(msg: String) {
        binding.tvLoadingText.text = msg
    }

    private fun startAnim() {
        binding.ivFolderAnim.setImageResource(R.drawable.anim_folder) // 关键：重新设置动画资源
        val anim = binding.ivFolderAnim.drawable as? AnimationDrawable
        anim?.start()
    }

    private fun stopAnim() {
        val anim = binding.ivFolderAnim.drawable as? AnimationDrawable
        anim?.stop()
    }

    private fun setDialogWidth(dp: Int) {
        val widthPx = context.resources.displayMetrics.density * dp
        window?.setLayout(widthPx.toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    }


}

