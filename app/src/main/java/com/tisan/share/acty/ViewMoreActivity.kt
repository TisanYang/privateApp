package com.tisan.share.acty

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tisan.share.dapter.ViewMoreFileAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.tisan.location.R
import com.tisan.location.databinding.ActivityViewMoreBinding
import com.tisan.share.base.BaseActivity
import com.tisan.share.data.FileRepository
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.datdabean.ModuleType
import com.tisan.share.ui.theme.dialog.ConfirmDialog
import com.tisan.share.utils.LogUtil
import com.tisan.share.vm.ViewMoreViewModel
import java.io.File

class ViewMoreActivity : BaseActivity<ActivityViewMoreBinding, ViewMoreViewModel>() {

    var fileList: MutableList<EncryptedFileItem> = mutableListOf()
    override val viewModelClass = ViewMoreViewModel::class.java

    var isInSelectionMode = false

    override fun inflateBinding() = ActivityViewMoreBinding.inflate(layoutInflater)

    override fun initListeners() {
        binding.btnBack.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun initViews() {

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(0, 0, 0, bottom)
            insets
        }
        binding.rvFiles.layoutManager =
            GridLayoutManager(this@ViewMoreActivity, 4)
        val adapter = ViewMoreFileAdapter(fileList)
        binding.rvFiles.adapter = adapter
        binding.btnSelectAll.setOnClickListener {
            if (!adapter.isInSelectionMode()) {
                // 如果还没进入选择模式，先进入
                adapter.isSelectionMode = true
                showBottomActionBar()
            }
            // 全选/取消全选切换
            val selectAll = !adapter.isAllSelected()
            adapter.toggleSelectAll(selectAll)
            // 更新按钮文字
            binding.btnSelectAll.text = if (selectAll) "取消全选" else "全选"
        }
        adapter.onItemClick = { item ->
            if (item.mimeType.startsWith("image/")) {

                val index = fileList.indexOfFirst { it.filePath == item.filePath }
                val intent = Intent(this@ViewMoreActivity, ImagePreviewActivity::class.java).apply {
                    putParcelableArrayListExtra("imageList", ArrayList(fileList))
                    putExtra("startIndex", index)
                }
                startActivity(intent)
            } else if (item.mimeType.startsWith("video/")) {

                val index = fileList.indexOfFirst { it.filePath == item.filePath }
                val intent = Intent(this@ViewMoreActivity, VideoPreviewActivity::class.java).apply {
                    putParcelableArrayListExtra("videoList", ArrayList(fileList))
                    putExtra("startIndex", index)
                }
                startActivity(intent)
            }
        }

        adapter.onItemLongClick = {
            showBottomActionBar()
            binding.btnSelectAll.text = if (adapter.isAllSelected()) "取消全选" else "全选"
        }

        binding.btnDelete.setOnClickListener {
//            val selected = adapter.getSelectedItems()
//            selected.forEach { File(it.filePath).delete() }
//            adapter.removeSelectedItems()
//            adapter.exitSelectionMode()
//            hideBottomActionBar()
            ConfirmDialog(
                context = this,
                message = "删除后将无法恢复，是否继续？",
                onConfirm = {
                    val successfullyDeleted = mutableListOf<EncryptedFileItem>()

                    val selected = adapter.getSelectedItems()
                    //删除文件本体
                    selected.forEach {
                        File(it.filePath).delete()
                        val encFile = File(it.filePath)
                        val metaFile = File(it.filePath + ".meta")

                        val deletedEnc = encFile.exists() && encFile.delete()
                        val deletedMeta = metaFile.exists() && metaFile.delete()

                        if (deletedEnc || deletedMeta) {
                            successfullyDeleted.add(it)
                        } else {
                            Log.w("Delete", "未能删除：${encFile.absolutePath} 或 ${metaFile.absolutePath}")
                        }

                    }
                    //删除文件标识
                    adapter.removeSelectedItems()
                    adapter.exitSelectionMode()
                    hideBottomActionBar()
                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show()
                }
            ).show()

        }

        binding.btnCancel.setOnClickListener {
            adapter.exitSelectionMode()
            hideBottomActionBar()
        }
    }

    override fun initData() {
//        val imageList: List<EncryptedFileItem>? =
//            if (intent.getStringExtra("TYPE").equals("IMAGE"))
//                intent.getParcelableArrayListExtra("imageList")
//            else intent.getParcelableArrayListExtra("videoList")

        val type = intent.getStringExtra("TYPE")?.let { ModuleType.fromString(it) }

        val imageList = FileRepository.getModulesByType(type)
        fileList.clear()
        imageList.forEach { moduleItem ->
            fileList.addAll(moduleItem.files)
        }
        binding.rvFiles.adapter?.notifyDataSetChanged()
    }

    // 显示底部操作栏
    private fun showBottomActionBar() {
        binding.bottomActionBar.visibility = View.VISIBLE
    }

    // 隐藏底部操作栏
    private fun hideBottomActionBar() {
        binding.bottomActionBar.visibility = View.GONE
        binding.btnSelectAll.text = "全选"
    }

}