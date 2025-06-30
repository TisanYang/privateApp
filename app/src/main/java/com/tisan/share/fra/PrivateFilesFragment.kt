package com.tisan.share.fra

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.tisan.location.R
import com.tisan.location.databinding.ActivityVaultBinding
import com.tisan.share.acty.ImagePreviewActivity
import com.tisan.share.acty.VideoPreviewActivity
import com.tisan.share.base.BaseFragment
import com.tisan.share.dapter.ModuleAdapter
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.vm.VaultViewModel
import com.tisan.share.vm.VaultViewModel.modules

class PrivateFilesFragment : BaseFragment<ActivityVaultBinding, VaultViewModel>() {

    override val viewModelClass = VaultViewModel::class.java
    private lateinit var moduleAdapter: ModuleAdapter
    private var allEncryptedImageFiles: List<EncryptedFileItem> = emptyList()
    private var allEncryptedVideoFiles: List<EncryptedFileItem> = emptyList()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityVaultBinding {
        return ActivityVaultBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        moduleAdapter = ModuleAdapter {
            // “查看更多”点击事件，跳转对应模块详情页
            //goToDetailPage(moduleType)
        }.apply {
            onFileItemClick = { item ->
                if (item.mimeType.startsWith("image/")) {

                    val index = allEncryptedImageFiles.indexOfFirst { it.filePath == item.filePath }
                    val intent = Intent(requireContext(), ImagePreviewActivity::class.java).apply {
                        putParcelableArrayListExtra("imageList", ArrayList(allEncryptedImageFiles))
                        putExtra("startIndex", index)
                    }
                    startActivity(intent)
                }else if (item.mimeType.startsWith("video/")){

                    val index = allEncryptedVideoFiles.indexOfFirst { it.filePath == item.filePath }
                    val intent = Intent(requireContext(), VideoPreviewActivity::class.java).apply {
                        putParcelableArrayListExtra("videoList", ArrayList(allEncryptedVideoFiles))
                        putExtra("startIndex", index)
                    }
                    startActivity(intent)
                }
            }
        }

        binding.moduleRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moduleAdapter
        }
    }

    override fun initData() {
        viewModel.modules.observe(this) { moduleList ->
            moduleAdapter.submitList(moduleList)

            allEncryptedImageFiles = moduleList
                .flatMap { it.files }
                .filter { it.mimeType.startsWith("image/") }

            allEncryptedVideoFiles = moduleList
                .flatMap { it.files }
                .filter { it.mimeType.startsWith("video/") }
        }
        viewModel.loadAllEncryptedFiles(context = requireContext())
    }

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                viewModel.importEncryptedFile(requireContext(), it)
            }
        }

    override fun observeData() {
        viewModel.modules.observe(viewLifecycleOwner) { modules ->
            // 更新UI
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
