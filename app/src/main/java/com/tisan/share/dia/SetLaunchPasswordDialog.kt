package com.tisan.share.dia

import android.content.Context
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.tisan.location.R
import com.tisan.location.databinding.DialogSetLaunchPasswordBinding
import com.tisan.share.ui.theme.dialog.BaseDialog
import com.tisan.share.utils.Constant
import com.tisan.share.utils.SecureSpHelper

class SetLaunchPasswordDialog(
    context: Context,
    var isChange: Boolean = false,
    private val onPasswordSet: (() -> Unit)? = null
) : BaseDialog<DialogSetLaunchPasswordBinding>(
    context,
    R.layout.dialog_set_launch_password,
    DialogSetLaunchPasswordBinding::inflate
) {
    private var lastClickTime = 0L
    private var showPwd = false
    private var showConfirm = false

    override fun initView() {

        setCancelable(false)

        binding.rlPasswordOld.visibility = if (isChange)  View.VISIBLE else View.GONE
        if (isChange){
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }
        // 显示密码切换

        binding.ivTogglePwdOld.setOnClickListener {
            showPwd = !showPwd
            updateInputType(binding.etPasswordOld, showPwd)
            binding.ivTogglePwdOld.setImageResource(if (showPwd) R.drawable.ic_eye_open else R.drawable.ic_eye_close)
        }

        binding.ivTogglePwd.setOnClickListener {
            showPwd = !showPwd
            updateInputType(binding.etPassword, showPwd)
            binding.ivTogglePwd.setImageResource(if (showPwd) R.drawable.ic_eye_open else R.drawable.ic_eye_close)
        }

        binding.ivToggleConfirm.setOnClickListener {
            showConfirm = !showConfirm
            updateInputType(binding.etConfirm, showConfirm)
            binding.ivToggleConfirm.setImageResource(if (showConfirm) R.drawable.ic_eye_open else R.drawable.ic_eye_close)
        }

        // 防快速点击 + 验证逻辑
        binding.btnConfirm.setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - lastClickTime < 1000) return@setOnClickListener
            lastClickTime = now

            val pwd = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirm.text.toString().trim()
            val oldPwd = binding.etPasswordOld.text.toString().trim()
            val savePwd = SecureSpHelper.getString(Constant.SP_VERIFY_KEY)

            when {
                (isChange && (oldPwd != savePwd)) -> {
                    showToast("原始启动码有误")
                }
                pwd.isEmpty() || confirm.isEmpty() -> showToast("请输入完整的启动码")
                !pwd.matches(Regex("^\\d{4,8}$")) -> showToast("启动码必须是4~8位数字")
                pwd != confirm -> showToast("两次输入的启动码不一致")
                else -> {
                    SecureSpHelper.putString(Constant.SP_VERIFY_KEY, pwd)
                    showToast("设置成功")
                    dismiss()
                    onPasswordSet?.invoke()
                }
            }
        }

        val targetEditText = if (isChange) binding.etPasswordOld else binding.etPassword
        targetEditText.postDelayed({
            targetEditText.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(targetEditText, InputMethodManager.SHOW_IMPLICIT)
        }, 300)

    }

    private fun updateInputType(editText: EditText, visible: Boolean) {
        val pos = editText.selectionStart
        editText.inputType = if (visible) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        editText.setSelection(pos)
    }

    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}
