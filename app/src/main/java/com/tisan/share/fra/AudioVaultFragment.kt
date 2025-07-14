package com.tisan.share.fra

import android.Manifest
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.tisan.location.databinding.FraAudioVaultBinding
import com.tisan.share.base.BaseFragment
import com.tisan.share.dapter.AudioRecordAdapter
import com.tisan.share.dia.AudioRecordBottomSheet
import com.tisan.share.vm.AudioVaultViewModel

/*class AudioVaultFragment : BaseFragment<FraAudioVaultBinding, AudioVaultViewModel>() {

    override val viewModelClass = AudioVaultViewModel::class.java
    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FraAudioVaultBinding.inflate(inflater, container, false)

    private val adapter = AudioRecordAdapter()

    override fun initView() {
        binding.rvRecordings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecordings.adapter = adapter
    }

    override fun observeData() {
        viewModel.filteredRecords.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    override fun initListeners() {
        binding.etSearch.addTextChangedListener {it ->
            viewModel.filter(it.toString())
        }

        binding.btnRecord.setOnClickListener {
            AudioRecordBottomSheet().show(parentFragmentManager, "RecordSheet")
        }
    }

    override fun initData() {
        viewModel.loadAllRecords()
    }
}*/

class AudioVaultFragment : BaseFragment<FraAudioVaultBinding, AudioVaultViewModel>() {

    override val viewModelClass = AudioVaultViewModel::class.java
    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FraAudioVaultBinding.inflate(inflater, container, false)

    private var adapter = AudioRecordAdapter()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun initView() {
        adapter = AudioRecordAdapter()
        binding.rvRecordings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecordings.adapter = adapter

        // 初始化权限请求器
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                showRecordBottomSheet()
            } else {
                Toast.makeText(requireContext(), "录音权限被拒绝，无法开始录音", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun initListeners() {
        binding.etSearch.addTextChangedListener {
            viewModel.filter(it.toString())
        }

        binding.btnRecord.setOnClickListener {
            checkAndRequestAudioPermission()
        }
    }

    private fun checkAndRequestAudioPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 已授权
                showRecordBottomSheet()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // 用户上次拒绝了，给予解释
                Toast.makeText(requireContext(), "需要录音权限才能使用该功能", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            else -> {
                // 第一次申请
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun showRecordBottomSheet() {
        val sheet = AudioRecordBottomSheet()
        sheet.onRecordSaved = {
            viewModel.loadAllRecords()
        }
        sheet.show(parentFragmentManager, "AudioRecord")
    }

    override fun observeData() {
        viewModel.filteredRecords.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    override fun initData() {
        viewModel.loadAllRecords()
    }
}

