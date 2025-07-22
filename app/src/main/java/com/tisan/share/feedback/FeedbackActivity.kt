package com.tisan.share.feedback

import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tisan.location.R
import com.tisan.location.databinding.ActivityFeedbackBinding
import com.tisan.share.base.BaseActivity


class FeedbackActivity : BaseActivity<ActivityFeedbackBinding, FeedbackViewModel>() {

    override val viewModelClass = FeedbackViewModel::class.java

    private lateinit var feedbackFragment: FeedbackFragment
    private lateinit var historyFragment: HistoryFragment

    override fun inflateBinding(): ActivityFeedbackBinding =
        ActivityFeedbackBinding.inflate(layoutInflater)

    override fun initViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_feedback)) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(0, 52, 0, bottom)
            insets
        }

        feedbackFragment = FeedbackFragment()
        historyFragment = HistoryFragment()

        // 加载反馈提交Fragment
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, feedbackFragment)
            .add(R.id.fragment_container, historyFragment)
            .hide(historyFragment) // 初始隐藏历史页
            .commit()
    }

    override fun initListeners() {
        binding.titleFeedback.setOnBackClick {
            finish()
        }

        binding.titleFeedback.setOnRightClickListener {
            // 右侧点击逻辑，比如跳转历史记录页
            Toast.makeText(this, "跳转历史反馈页", Toast.LENGTH_SHORT).show()
            switchToHistoryFragment()
        }
    }

    fun switchToFeedbackFragment(question: String) {
        supportFragmentManager.beginTransaction()
            .hide(historyFragment)
            .show(feedbackFragment)
            .commit()

        binding.titleFeedback.showRightText()
    }

    private fun switchToHistoryFragment() {
        supportFragmentManager.beginTransaction()
            .hide(feedbackFragment)
            .show(historyFragment)
            .commit()

        binding.titleFeedback.hideRightText()
    }


}
