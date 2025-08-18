package com.tisan.share.ui.theme.dialog

import android.content.Context
import com.kj.infinite.R
import com.kj.infinite.databinding.DialogConfirmBinding

class ConfirmDialog(
    context: Context,
    private val message: String,
    private val onConfirm: () -> Unit,
    private val onCancel: (() -> Unit)? = null
) : BaseDialog<DialogConfirmBinding>(
    context,
    R.layout.dialog_confirm,
    DialogConfirmBinding::inflate
) {
    override fun initView() {
        binding.tvMessage.text = message

        binding.btnConfirm.setOnClickListener {
            dismiss()
            onConfirm()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
            onCancel?.invoke()
        }
    }
}
