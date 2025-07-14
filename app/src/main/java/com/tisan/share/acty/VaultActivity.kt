package com.tisan.share.acty

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tisan.location.databinding.ActivityVaultBinding
import com.tisan.share.base.BaseActivity
import com.tisan.share.dapter.ModuleAdapter
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.datdabean.FileModuleItem
import com.tisan.share.datdabean.ModuleType
import com.tisan.share.vm.VaultViewModel

class VaultActivity : BaseActivity<ActivityVaultBinding, VaultViewModel>() {

    private lateinit var moduleAdapter: ModuleAdapter
    override val viewModelClass = VaultViewModel::class.java


    override fun inflateBinding(): ActivityVaultBinding =
        ActivityVaultBinding.inflate(layoutInflater)

    override fun initViews() {
        moduleAdapter = ModuleAdapter()

        binding.moduleRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@VaultActivity)
            adapter = moduleAdapter
        }

    }

    override fun initData() {
        viewModel = ViewModelProvider(this)[VaultViewModel::class.java]

        viewModel.modules.observe(this) { moduleList ->
            moduleAdapter.submitList(moduleList)
        }
        viewModel.loadAllEncryptedFiles(this)
        // 加载数据
    }


    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                //viewModel.importEncryptedFile(this, it)
            }
        }

    override fun initListeners() {
        with(binding) {
            addFileButton.setOnClickListener {
                filePickerLauncher.launch(arrayOf("*/*")) // 选择所有文件
            }
        }
    }
}