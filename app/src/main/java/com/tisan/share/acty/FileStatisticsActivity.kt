package com.tisan.share.acty

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tisan.location.R
import com.tisan.location.databinding.ActivityFileStatisticsBinding
import com.tisan.share.base.BaseActivity
import com.tisan.share.vm.FileStatisticsViewModel

class FileStatisticsActivity :
    BaseActivity<ActivityFileStatisticsBinding, FileStatisticsViewModel>() {

    override val viewModelClass = FileStatisticsViewModel::class.java

    override fun inflateBinding(): ActivityFileStatisticsBinding  = ActivityFileStatisticsBinding.inflate(layoutInflater)

    override fun initViews() {

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_file_sta)) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(0, 0, 0, bottom)
            insets
        }
    }

    override fun initData() {

    }

    override fun initListeners() {

    }
}