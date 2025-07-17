package com.tisan.share.acty

import android.content.Intent
import android.os.Bundle
import com.tisan.location.databinding.ActivityCalculatorBinding
import com.tisan.share.base.BaseActivity
import com.tisan.share.vm.SimpleViewModel
import net.objecthunter.exp4j.ExpressionBuilder

class CalculatorActivity : BaseActivity<ActivityCalculatorBinding, SimpleViewModel>() {

    override val viewModelClass = SimpleViewModel::class.java

    override fun inflateBinding(): ActivityCalculatorBinding =
        ActivityCalculatorBinding.inflate(layoutInflater)

    private var input = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                if (input == "111") {
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
    }

    private fun append(value: String) {
        input += value
        binding.result.text = input
    }
}
