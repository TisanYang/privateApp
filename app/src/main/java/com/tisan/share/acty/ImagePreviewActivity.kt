package com.tisan.share.acty

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.tisan.location.databinding.ActivityImagePreviewBinding
import com.tisan.share.base.BaseActivity
import com.tisan.share.dapter.ImagePreviewAdapter
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.utils.CryptoUtil
import com.tisan.share.vm.ImagePreviewViewModel
import com.tisan.share.vm.ImagePreviewViewModel.showToast
import java.io.File

class ImagePreviewActivity : BaseActivity<ActivityImagePreviewBinding, ImagePreviewViewModel>() {
    override val viewModelClass = ImagePreviewViewModel::class.java

    private lateinit var imageList: List<EncryptedFileItem>
    private var startIndex: Int = 0


    override fun inflateBinding(): ActivityImagePreviewBinding =
        ActivityImagePreviewBinding.inflate(layoutInflater)


    override fun initViews() {
//        val path = intent.getStringExtra("filePath") ?: return
//
//        val decryptedBytes = try {
//            CryptoUtil.decrypt(File(path).readBytes())
//        } catch (e: Exception) {
//            showToast("图片解密失败")
//            finish()
//            return
//        }
//
//        val bitmap = BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)
//        binding.imageView.setImageBitmap(bitmap)
//
//        // 点击图片退出
//        binding.imageView.setOnClickListener {
//            finish()
//        }
    }

    override fun initData() {
        // 接收传入数据
        imageList = intent.getParcelableArrayListExtra("imageList") ?: emptyList()
        startIndex = intent.getIntExtra("startIndex", 0)

        val adapter = ImagePreviewAdapter(imageList).apply {
            onItemClick = {
                finish()
            }
        }
        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(startIndex, false)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    Toast.makeText(this@ImagePreviewActivity, "已经是第一张", Toast.LENGTH_SHORT)
                        .show()
                } else if (position == imageList.size - 1) {
                    Toast.makeText(this@ImagePreviewActivity, "已经是最后一张", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }
}


