package com.tisan.share.ui.theme.dialog

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import com.kj.infinite.R
import com.kj.infinite.databinding.DialogPermissionBinding
import com.tisan.share.acty.WebViewActivity

class PermissionDialog(
    context: Context,
    private val onConfirm: () -> Unit,
    private val onCancel: () -> Unit
) : BaseDialog<DialogPermissionBinding>(
    context,
    R.layout.dialog_permission,
    DialogPermissionBinding::inflate
) {

    override fun initView() {
        setCancelable(false)

        // 设置标题
        binding.tvTitle.text = "权限申请说明"

        this.setCanceledOnTouchOutside(false)

        // 设置富文本正文
        val content = "我们非常重视您的个人信息保护。在使用App前，请仔细阅读《用户协议》和《隐私政策》。"
        val spannable = SpannableString(content)

        val userAgreement = "《用户协议》"
        val privacyPolicy = "《隐私政策》"

        val userStart = content.indexOf(userAgreement)
        val userEnd = userStart + userAgreement.length
        val privacyStart = content.indexOf(privacyPolicy)
        val privacyEnd = privacyStart + privacyPolicy.length

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                openWeb(context, "https://example.com/user")
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#4285F4")
                ds.isUnderlineText = false
            }
        }, userStart, userEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                openWeb(context, "https://example.com/privacy")
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#4285F4")
                ds.isUnderlineText = false
            }
        }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvContent.text = spannable
        binding.tvContent.movementMethod = LinkMovementMethod.getInstance()

        // 设置按钮
        binding.btnCancel.setOnClickListener { onCancel(); dismiss() }
        binding.btnConfirm.setOnClickListener { onConfirm(); dismiss() }
    }

    private fun openWeb(context: Context, url: String) {
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//        context.startActivity(intent)
        val intent = Intent(context, WebViewActivity::class.java)
        intent.putExtra("url", "https://www.baidu.com")
        context.startActivity(intent)

    }
}
