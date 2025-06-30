package com.tisan.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.tisan.location.databinding.ActivityDialerBinding
import com.tisan.share.acty.VaultActivity
import com.tisan.share.base.BaseActivity
import com.tisan.share.vm.SimpleViewModel

class DialerEntryActivity :  BaseActivity<ActivityDialerBinding, SimpleViewModel>() {
    override val viewModelClass = SimpleViewModel::class.java

    //private lateinit var binding:ActivityDialerBinding

    override fun inflateBinding(): ActivityDialerBinding = ActivityDialerBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding = ActivityDialerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding){
           btnFile.setOnClickListener {
               startActivity(Intent(this@DialerEntryActivity, VaultActivity::class.java))
           }
        }

    }

}