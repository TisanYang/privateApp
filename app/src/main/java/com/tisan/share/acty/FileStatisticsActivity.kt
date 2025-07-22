package com.tisan.share.acty

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tisan.location.R
import com.tisan.location.databinding.ActivityFileStatisticsBinding
import com.tisan.share.base.BaseActivity
import com.tisan.share.data.FileRepository
import com.tisan.share.datdabean.ModuleType
import com.tisan.share.vm.FileStatisticsViewModel
import java.io.File

class FileStatisticsActivity :
    BaseActivity<ActivityFileStatisticsBinding, FileStatisticsViewModel>() {

    override val viewModelClass = FileStatisticsViewModel::class.java

    override fun inflateBinding(): ActivityFileStatisticsBinding =
        ActivityFileStatisticsBinding.inflate(layoutInflater)

    override fun initViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_file_sta)) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(0, 0, 0, bottom)
            insets
        }
    }


    override fun onResume() {
        super.onResume()

        val moduleSizes: Map<ModuleType, Long> = FileRepository.getFullModules()
            .associate { moduleItem ->
                val totalSize = moduleItem.files.sumOf { file ->
                    val fileObj = File(file.filePath)
                    if (fileObj.exists()) fileObj.length() else 0L
                }
                moduleItem.type to totalSize
            }


        binding.itemImage.apply {
            ivIcon.setImageResource(R.drawable.ic_placeholder_left)
            tvLabel.text = "图片"
            tvCount.text = formatModuleCountAndSize(ModuleType.IMAGE, "张", moduleSizes)
            tvPercent.text = getModuleSizePercentText(ModuleType.IMAGE,moduleSizes)
        }

        binding.itemVideo.apply {
            ivIcon.setImageResource(R.drawable.ic_placeholder_left)
            tvLabel.text = "视频"
            tvCount.text = formatModuleCountAndSize(ModuleType.VIDEO, "个", moduleSizes)
            tvPercent.text = getModuleSizePercentText(ModuleType.VIDEO,moduleSizes)

        }

        binding.itemAudio.apply {
            ivIcon.setImageResource(R.drawable.ic_placeholder_left)
            tvLabel.text = "音频"
            tvCount.text = formatModuleCountAndSize(ModuleType.AUDIO, "条", moduleSizes)
            tvPercent.text = getModuleSizePercentText(ModuleType.AUDIO,moduleSizes)
        }

        binding.itemDocument.apply {
            ivIcon.setImageResource(R.drawable.ic_placeholder_left)
            tvLabel.text = "文档"
            tvCount.text = formatModuleCountAndSize(ModuleType.DOCUMENT, "个", moduleSizes)
            tvPercent.text = getModuleSizePercentText(ModuleType.DOCUMENT,moduleSizes)
        }

        val totalCount = FileRepository.getFullModules().sumOf { it.files.size }
        binding.itemTotal.apply {
            ivIcon.setImageResource(R.drawable.ic_placeholder_left)
            tvLabel.text = "总计"
            tvCount.text = "" + totalCount + " 个" + formatSize(moduleSizes.values.sum())
        }

    }
    fun formatSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.2f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.2f MB", mb)
        val gb = mb / 1024.0
        return String.format("%.2f GB", gb)
    }

    // 格式化显示文本，参数：模块类型、单位名称、大小Map
    private fun formatModuleCountAndSize(
        moduleType: ModuleType,
        unitName: String,
        moduleSizes: Map<ModuleType, Long>
    ): String {
        val count = FileRepository.getModulesByType(moduleType)[0].files.size
        val sizeText = moduleSizes[moduleType]?.let { formatSize(it) } ?: "0 B"
        return "$count $unitName，$sizeText"
    }

    fun getModuleSizePercentText(
        moduleType: ModuleType,
        moduleSizes: Map<ModuleType, Long>
    ): String {
        val totalSize = moduleSizes.values.sum()
        if (totalSize == 0L) return "0.00%"
        val moduleSize = moduleSizes[moduleType] ?: 0L
        val percent = moduleSize.toDouble() / totalSize * 100
        return String.format("%.2f%%", percent)
    }



}