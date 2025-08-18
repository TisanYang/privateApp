package com.tisan.share.acty

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tisan.location.databinding.ActivityCalculatorBinding
import com.tisan.share.base.BaseActivity
import com.tisan.share.dia.SetLaunchPasswordDialog
import com.tisan.share.ui.theme.dialog.PermissionDialog
import com.tisan.share.utils.ApkUpdateHelper
import com.tisan.share.utils.Constant
import com.tisan.share.utils.NetworkUtil
import com.tisan.share.utils.SecureSpHelper
import com.tisan.share.vm.SimpleViewModel
import net.objecthunter.exp4j.ExpressionBuilder

class CalculatorActivity : BaseActivity<ActivityCalculatorBinding, SimpleViewModel>() {

    override val viewModelClass = SimpleViewModel::class.java

    override fun inflateBinding(): ActivityCalculatorBinding =
        ActivityCalculatorBinding.inflate(layoutInflater)

    private var input = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        window.statusBarColor = Color.TRANSPARENT
        setContentView(binding.root)

        // 所有按钮绑定监听
        with(binding) {
            btn0.setOnClickListener { append("0") }
            btn1.setOnClickListener { append("1") }
            btn2.setOnClickListener { append("2") }
            btn3.setOnClickListener { append("3") }
            btn4.setOnClickListener { append("4") }
            btn5.setOnClickListener { append("5") }
            btn6.setOnClickListener { append("6") }
            btn7.setOnClickListener { append("7") }
            btn8.setOnClickListener { append("8") }
            btn9.setOnClickListener { append("9") }

            btnAdd.setOnClickListener { append("+") }
            btnSub.setOnClickListener { append("-") }
            btnMul.setOnClickListener { append("*") }
            btnDiv.setOnClickListener { append("/") }
            btnDot.setOnClickListener { append(".") }

            btnClear.setOnClickListener {
                input = ""
                binding.result.text = ""
            }

            btnEqual.setOnClickListener {
//

                if (input == SecureSpHelper.getString(Constant.SP_VERIFY_KEY)) {
                    // 密码匹配，进入主界面
                    //Toast.makeText(this@CalculatorActivity, "进入隐私空间", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@CalculatorActivity, MainActivity::class.java))
                    finish()
                } else {
                    try {
                        val expr = ExpressionBuilder(input).build()
                        val output = expr.evaluate()
                        result.text = output.toString()
                        input = output.toString()
                    } catch (e: Exception) {
                        result.text = "错误"
                        input = ""
                    }
                }
            }
        }

//        if (!NetworkUtil.isNetworkConnected(this)) {
//            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
//            return
//        }

        if (SecureSpHelper.getString(Constant.SP_VERIFY_KEY).isNullOrEmpty()) {
            SetLaunchPasswordDialog(this) {
                // 密码设置成功后的动作

            }.apply {
                setCancelable(false)
            }.show()
        }

        // SecureSpHelper.putInt(Constant.SP_POLICY_AGREE, 0)
        //未同意隐私政策和用户协议
        if (SecureSpHelper.getInt(Constant.SP_POLICY_AGREE, 0) == 0) {
            PermissionDialog(this,
                onConfirm = {
                    // ✅ 用户点击“同意”后的处理逻辑
                    SecureSpHelper.putInt(Constant.SP_POLICY_AGREE, 1)
                    // 继续启动主界面/后续逻辑
                },
                onCancel = {
                    // ❌ 用户点击“取消”后的处理逻辑
                    Toast.makeText(this, "未同意协议，无法继续使用", Toast.LENGTH_SHORT).show()
                    finish() // 退出 App 或拦截继续操作
                })
                .show()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ApkUpdateHelper.handlePermissionResult(requestCode, grantResults) {
            // Permission granted, retry download
            ApkUpdateHelper.downloadAndInstallApk(
                context = this,
                apkUrl = "https://120.77.93.183:10658/down/j1oyUNkgnpEc.apk"
            )
        }
    }


    private fun append(value: String) {
        input += value
        binding.result.text = input
    }
}
