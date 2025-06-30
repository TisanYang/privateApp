package com.tisan.share

import android.content.Intent
import android.os.Bundle
import com.tisan.location.databinding.ActivityMainBinding
import com.tisan.share.acty.CalculatorActivity
import com.tisan.share.base.BaseActivity
import com.tisan.share.vm.SimpleViewModel

class LauncherActivity : BaseActivity<ActivityMainBinding,SimpleViewModel>() {

    override val viewModelClass = SimpleViewModel::class.java

    override fun inflateBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding  = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 调试用，直接跳转主界面
//        with(binding) {
//            btn.setOnClickListener {
//                startActivity(Intent(this@LauncherActivity, CalculatorActivity::class.java))
//            }
//        }
    }
}