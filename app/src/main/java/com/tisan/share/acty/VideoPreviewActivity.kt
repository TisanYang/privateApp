package com.tisan.share.acty

import androidx.viewpager2.widget.ViewPager2
import com.kj.infinite.R
import com.kj.infinite.databinding.ActivityVideoPreviewBinding
import com.tisan.share.base.BaseActivity
import com.tisan.share.dapter.VideoPreviewAdapter
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.vm.VideoPreviewViewModel

class VideoPreviewActivity : BaseActivity<ActivityVideoPreviewBinding, VideoPreviewViewModel>() {

    private lateinit var videoItems: List<EncryptedFileItem>
    private var startIndex: Int = 0

    override fun initViews() {
        videoItems = intent.getParcelableArrayListExtra("videoList") ?: emptyList()
        startIndex = intent.getIntExtra("startIndex", 0)

        val adapter = VideoPreviewAdapter(this, videoItems)
        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(startIndex, false)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> viewModel.showToast("已经是第一段视频")
                    videoItems.size - 1 -> viewModel.showToast("已经是最后一段视频")
                }
            }
        })
    }

    override val viewModelClass = VideoPreviewViewModel::class.java

    override fun inflateBinding(): ActivityVideoPreviewBinding = ActivityVideoPreviewBinding.inflate(layoutInflater)
}
