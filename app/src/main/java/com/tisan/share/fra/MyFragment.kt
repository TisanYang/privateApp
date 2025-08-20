package com.tisan.share.fra

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kj.infinite.R
import com.kj.infinite.databinding.FraMyBinding
import com.tisan.share.acty.FileStatisticsActivity
import com.tisan.share.acty.ModifyDisplayActivity
import com.tisan.share.base.BaseFragment
import com.tisan.share.dia.SetLaunchPasswordDialog
import com.tisan.share.feedback.FeedbackActivity
import com.tisan.share.net.RetrofitClient
import com.tisan.share.net.requestbean.CommentRequest
import com.tisan.share.utils.LauncherAliasSwitcher
import com.tisan.share.vm.MineViewModel
import com.tisan.share.vm.SimpleViewModel

class MyFragment : BaseFragment<FraMyBinding, MineViewModel>() {
    override val viewModelClass = MineViewModel::class.java

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FraMyBinding {
        return FraMyBinding.inflate(inflater, container, false)
    }

    override fun initData() {
        viewModel.commentState.observe(viewLifecycleOwner){
            Toast.makeText(context, "" + it?.id, Toast.LENGTH_SHORT).show()
        }
    }
    override fun initListeners() {
        binding.llPrivacySpace.setOnClickListener {
            startActivity(Intent(activity, FileStatisticsActivity::class.java))
        }

        binding.llPasswordLock.setOnClickListener {
            SetLaunchPasswordDialog(requireContext(),true) {
                // 密码设置成功后的动作

            }.show()
        }

        binding.llFeedback.setOnClickListener {
            startActivity(Intent(activity, FeedbackActivity::class.java))
        }

        binding.llChangeInfo.setOnClickListener {

            viewModel.submitComment()

            //startActivity(Intent(activity, ModifyDisplayActivity::class.java))
        }

    }

}