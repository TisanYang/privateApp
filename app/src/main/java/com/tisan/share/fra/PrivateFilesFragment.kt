package com.tisan.share.fra

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.tisan.location.databinding.ActivityVaultBinding
import com.tisan.share.acty.ImagePreviewActivity
import com.tisan.share.acty.VideoPreviewActivity
import com.tisan.share.acty.ViewMoreActivity
import com.tisan.share.base.BaseFragment
import com.tisan.share.dapter.ModuleAdapter
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.datdabean.ModuleType
import com.tisan.share.utils.EventBus
import com.tisan.share.vm.VaultViewModel

class PrivateFilesFragment : BaseFragment<ActivityVaultBinding, VaultViewModel>() {

    private var importDialog: AlertDialog? = null

    companion object {
        private const val REQUEST_CODE_VIEW_MORE = 1001
    }

    override val viewModelClass = VaultViewModel::class.java
    private lateinit var moduleAdapter: ModuleAdapter
    private var allEncryptedImageFiles: List<EncryptedFileItem> = emptyList()
    private var allEncryptedVideoFiles: List<EncryptedFileItem> = emptyList()

    private var totalEncryptedImageFiles: List<EncryptedFileItem> = emptyList()
    private var totalEncryptedVideoFiles: List<EncryptedFileItem> = emptyList()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityVaultBinding {
        return ActivityVaultBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        moduleAdapter = ModuleAdapter().apply {
            onFileItemClick = { item ->
                if (item.mimeType.startsWith("image/")) {

                    val index = allEncryptedImageFiles.indexOfFirst { it.filePath == item.filePath }
                    val intent = Intent(requireContext(), ImagePreviewActivity::class.java).apply {
                        putParcelableArrayListExtra("imageList", ArrayList(allEncryptedImageFiles))
                        putExtra("startIndex", index)
                    }
                    startActivity(intent)
                } else if (item.mimeType.startsWith("video/")) {

                    val index = allEncryptedVideoFiles.indexOfFirst { it.filePath == item.filePath }
                    val intent = Intent(requireContext(), VideoPreviewActivity::class.java).apply {
                        putParcelableArrayListExtra("videoList", ArrayList(allEncryptedVideoFiles))
                        putExtra("startIndex", index)
                    }
                    startActivity(intent)
                }
            }

            onSeeMoreClick = { item ->
                if (item.type == ModuleType.IMAGE) {
                    val intent = Intent(requireContext(), ViewMoreActivity::class.java).apply {
                        putExtra("TYPE", "IMAGE")
                    }
                    startActivityForResult(intent, REQUEST_CODE_VIEW_MORE)
                } else if (item.type == ModuleType.VIDEO) {
                    val intent = Intent(requireContext(), ViewMoreActivity::class.java).apply {
                        putExtra("TYPE", "VIDEO")
                    }
                    startActivityForResult(intent, REQUEST_CODE_VIEW_MORE)
                }
            }
        }

        binding.moduleRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moduleAdapter
        }
    }

    override fun initData() {

        viewModel.modules.observe(viewLifecycleOwner) { moduleList ->
            moduleAdapter.submitList(moduleList)

            allEncryptedImageFiles = moduleList
                .flatMap { it.files }
                .filter { it.mimeType.startsWith("image/") }

            allEncryptedVideoFiles = moduleList
                .flatMap { it.files }
                .filter { it.mimeType.startsWith("video/") }
        }
        viewModel.fullData.observe(viewLifecycleOwner) { moduleList ->
            totalEncryptedImageFiles = moduleList
                .flatMap { it.files }
                .filter { it.mimeType.startsWith("image/") }

            totalEncryptedVideoFiles = moduleList
                .flatMap { it.files }
                .filter { it.mimeType.startsWith("video/") }
        }


        viewModel.loadAllEncryptedFiles(context = requireContext())

        // 在 Fragment 的 onViewCreated 或合适生命周期方法内
        lifecycleScope.launchWhenStarted {
            viewModel.importProgress.collect { progress ->
                if (progress == null) {
                    // 导入结束，隐藏进度UI
                    importDialog?.dismiss()
                    importDialog = null
                } else {
                    if (importDialog == null) {
                        importDialog = AlertDialog.Builder(requireContext())
                            .setTitle("导入中...")
                            .setMessage("已导入 ${progress.current} / ${progress.total}")
                            .setCancelable(false)
                            .create()
                        importDialog?.show()
                    } else {
                        importDialog?.setMessage("已导入 ${progress.current} / ${progress.total}")
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.importResult.collect { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                EventBus.fileListUpdated.collect {
                    viewModel.loadAllEncryptedFiles(requireContext())
                }
            }
        }

    }

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->

            if (uris.isNotEmpty()) {
                viewModel.importEncryptedFiles(requireContext(), uris)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_VIEW_MORE && resultCode == Activity.RESULT_OK) {
            viewModel.loadAllEncryptedFiles(context = requireContext())
        }
    }
}

