package com.tisan.share.fra

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tisan.location.R
import com.tisan.location.databinding.FraMyBinding
import com.tisan.share.acty.FileStatisticsActivity
import com.tisan.share.base.BaseFragment
import com.tisan.share.feedback.FeedbackActivity
import com.tisan.share.vm.SimpleViewModel

class MyFragment : BaseFragment<FraMyBinding, SimpleViewModel>() {
    override val viewModelClass = SimpleViewModel::class.java

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FraMyBinding {
        return FraMyBinding.inflate(inflater, container, false)
    }

    override fun initListeners() {
        binding.llPrivacySpace.setOnClickListener {
            startActivity(Intent(activity, FileStatisticsActivity::class.java))
        }

        binding.llFeedback.setOnClickListener {
            startActivity(Intent(activity, FeedbackActivity::class.java))
        }
    }

}