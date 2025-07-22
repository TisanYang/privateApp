package com.tisan.share.fra

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tisan.location.databinding.FraFunctionBinding
import com.tisan.share.base.BaseFragment
import com.tisan.share.vm.FunctionViewModel
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import androidx.activity.result.contract.ActivityResultContracts.TakeVideo
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.tisan.share.acty.AudioVaultActivity
import com.tisan.share.utils.EventBus
import com.tisan.share.utils.LogUtil
import com.tisan.share.vm.SharedEventViewModel
import kotlinx.coroutines.launch


class FunctionFragment : BaseFragment<FraFunctionBinding, FunctionViewModel>() {

    override val viewModelClass = FunctionViewModel::class.java

//    private val sharedViewModel: SharedEventViewModel by activityViewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FraFunctionBinding {
        return FraFunctionBinding.inflate(inflater, container, false)
    }

    private lateinit var tempPhotoFile: File
    private lateinit var tempVideoFile: File
    private lateinit var videoUri: Uri

    // 定义两个 launcher，用于申请 CAMERA 权限
    private val cameraPermissionLauncherForPhoto =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startTakePhoto()
            } else {
                Toast.makeText(requireContext(), "请授予相机权限以拍照", Toast.LENGTH_SHORT).show()
            }
        }

    private val cameraPermissionLauncherForVideo =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startTakeVideo()
            } else {
                Toast.makeText(requireContext(), "请授予相机权限以录像", Toast.LENGTH_SHORT).show()
            }
        }


    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val uri = Uri.fromFile(tempPhotoFile)
            viewModel.importEncryptedFile(requireContext(), uri)
        } else {
            Toast.makeText(requireContext(), "取消拍照", Toast.LENGTH_SHORT).show()
        }
    }


    // 拍视频回调，返回缩略图 Bitmap?
//    private val takeVideoLauncher = registerForActivityResult(TakeVideo()) { bitmap: Bitmap? ->
//        // 录像完成后，判断文件是否存在且大小 > 0，作为成功判断
//        val file = File(videoUri.path ?: "")
//        if (file.exists() && file.length() > 0) {
//            viewModel.importEncryptedFile(requireContext(), videoUri)
//        } else {
//            Toast.makeText(requireContext(), "取消录像或录制失败", Toast.LENGTH_SHORT).show()
//        }
//    }
//    private val takeVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val file = File(videoUri.path ?: "")
//            if (file.exists() && file.length() > 0) {
//                viewModel.importEncryptedFile(requireContext(), videoUri)
//            } else {
//                Toast.makeText(requireContext(), "录像失败，文件为空", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            Toast.makeText(requireContext(), "取消录像", Toast.LENGTH_SHORT).show()
//        }
//    }

    private val takeVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: videoUri

            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                if (inputStream != null && inputStream.available() > 0) {
                    viewModel.importEncryptedFile(requireContext(), uri)
                } else {
                    Toast.makeText(requireContext(), "录像失败，无内容", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "读取录像失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "取消录像", Toast.LENGTH_SHORT).show()
        }
    }







    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startTakePhoto()
            } else {
                cameraPermissionLauncherForPhoto.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnTakeVideo.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startTakeVideo()
            } else {
                cameraPermissionLauncherForVideo.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnRecordAudio.setOnClickListener {
            startActivity(Intent(requireActivity(), AudioVaultActivity::class.java))
        }

        // 导入进度监听
        lifecycleScope.launchWhenStarted {
            viewModel.importProgress.collectLatest { progress ->
                binding.progressBar.isVisible = progress != null
                progress?.let {
                    binding.progressBar.progress = (it.current * 100 / it.total)
                }
            }
        }

        // 导入结果提示
        lifecycleScope.launchWhenStarted {
            viewModel.importResult.collectLatest { result ->
                if (result.isNotBlank()) {
                    Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startTakePhoto() {
        tempPhotoFile = File.createTempFile("photo_", ".jpg", requireContext().cacheDir)
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            tempPhotoFile
        )
        takePhotoLauncher.launch(uri)
    }

//    private fun startTakeVideo() {
//        tempVideoFile = File.createTempFile("video_", ".mp4", requireContext().cacheDir)
//        videoUri = FileProvider.getUriForFile(
//            requireContext(),
//            "${requireContext().packageName}.fileprovider",
//            tempVideoFile
//        )
//        takeVideoLauncher.launch(videoUri)
//    }


    private fun startTakeVideo() {
        tempVideoFile = File.createTempFile("video_", ".mp4", requireContext().cacheDir)
        videoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            tempVideoFile
        )
        LogUtil.d("videoUri", videoUri.toString())

        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        takeVideoLauncher.launch(intent)
    }


}
