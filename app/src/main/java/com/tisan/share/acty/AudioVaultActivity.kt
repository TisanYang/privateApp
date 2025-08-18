package com.tisan.share.acty

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kj.infinite.R
import com.kj.infinite.databinding.ActivityAudioVaultBinding
import com.tisan.share.base.BaseActivity
import com.tisan.share.fra.AudioVaultFragment
import com.tisan.share.vm.AudioVaultViewModel
import dagger.hilt.android.AndroidEntryPoint


class AudioVaultActivity : BaseActivity<ActivityAudioVaultBinding, AudioVaultViewModel>() {

    override val viewModelClass = AudioVaultViewModel::class.java


    override fun inflateBinding() = ActivityAudioVaultBinding.inflate(layoutInflater)

    override fun initViews() {

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container)) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(0, 0, 0, bottom)
            insets
        }

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, AudioVaultFragment())
            .commitNow()
    }
}
