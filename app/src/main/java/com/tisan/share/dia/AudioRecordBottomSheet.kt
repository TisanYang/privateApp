package com.tisan.share.dia

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tisan.location.databinding.DialogAudioRecordBinding
import com.tisan.share.utils.CryptoUtil
import com.tisan.share.vm.AudioRecordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.random.Random

class AudioRecordBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogAudioRecordBinding? = null
    private val binding get() = _binding!!

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var timerJob: Job? = null
    private var startTime = 0L

    private val waveBars = mutableListOf<View>()
    private val animators = mutableListOf<ValueAnimator>()
    private var micAnimator: ObjectAnimator? = null
    private var isRecording = false
    private val barCount = 45

    private lateinit var viewModel: AudioRecordViewModel

    var onRecordSaved: (() -> Unit)? = null // 外部刷新用 callback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAudioRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AudioRecordViewModel(requireContext().applicationContext) as T
            }
        })[AudioRecordViewModel::class.java]

        setupFakeWaveBars()

        binding.btnStart.setOnClickListener { toggleRecording() }
        binding.btnPause.setOnClickListener { pauseRecording() }
        binding.btnStop.setOnClickListener { stopRecording() }

        updateUI("准备录音", "00:00")
    }

    private fun toggleRecording() {
        if (!isRecording) {
            startRecording()
        } else {
            pauseRecording()
        }
    }

    private fun startRecording() {
        val context = requireContext()
        val fileName = "record_${System.currentTimeMillis()}.m4a"
        audioFile = File(context.cacheDir, fileName) // 先写缓存

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile!!.absolutePath)
            prepare()
            start()
        }

        startTime = System.currentTimeMillis()
        startTimer()

        startFakeWaveAnimation()
        startMicBreathAnimation()

        updateUI("录音中...", "00:00")
        isRecording = true
    }

    private fun pauseRecording() {
        recorder?.stop()
        recorder?.release()
        recorder = null

        stopMicBreathAnimation()
        stopFakeWaveAnimation()
        timerJob?.cancel()

        updateUI("已暂停", binding.tvRecordTimer.text.toString())
        isRecording = false
    }

    private fun stopRecording() {
        if (isRecording) pauseRecording()

        val origin = audioFile ?: return
        val targetDir = File(requireContext().filesDir, "vault_audio").apply { mkdirs() }
        val target = File(targetDir, origin.name.replace(".m4a", ".enc"))

        try {
            origin.inputStream().use { input ->
                target.outputStream().use { output ->
                    CryptoUtil.encrypt(input, output)
                }
            }

            // 删除原始录音文件
            if (!origin.delete()) {
                Log.w("AudioRecord", "源录音文件删除失败: ${origin.absolutePath}")
            }

            // ✅ 调用 VM 入库逻辑
            lifecycleScope.launch {
                viewModel.importSingleFile(requireContext(), Uri.fromFile(target))
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "加密失败: ${e.message}", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), "录音已加密保存", Toast.LENGTH_SHORT).show()
        onRecordSaved?.invoke()
        dismiss()
    }


    private fun startTimer() {
        timerJob = lifecycleScope.launch(Dispatchers.Main) {
            while (isActive) {
                val duration = System.currentTimeMillis() - startTime
                val sec = duration / 1000
                val min = sec / 60
                val s = sec % 60
                val timeStr = String.format("%02d:%02d", min, s)
                binding.tvRecordTimer.text = timeStr
                delay(1000)
            }
        }
    }

    private fun updateUI(status: String, time: String) {
        binding.tvRecordStatus.text = status
        binding.tvRecordTimer.text = time
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopFakeWaveAnimation()
        stopMicBreathAnimation()
        timerJob?.cancel()
        recorder?.release()
        _binding = null
    }

    private fun setupFakeWaveBars() {
        binding.waveContainer.removeAllViews()
        waveBars.clear()
        animators.clear()

        val barWidth = 1.dp()
        val barMargin = 2.dp()
        val staticHeight = 45.dp()  // 所有柱子初始高度相同

        repeat(barCount) {
            val wrapper = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    barWidth + 2 * barMargin, 120.dp()
                )
            }

            val bar = View(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(barWidth, staticHeight, Gravity.CENTER)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = barWidth / 2f
                    setColor(Color.BLACK)
                }
                alpha = 0.9f
            }

            wrapper.addView(bar)
            binding.waveContainer.addView(wrapper)
            waveBars.add(bar)
        }
    }

    private fun startFakeWaveAnimation() {
        animators.forEach { it.cancel() }
        animators.clear()

        waveBars.forEach { bar ->
            val minH = 30.dp()
            val maxH = 100.dp()
            val animator = ValueAnimator.ofInt(minH, maxH, minH).apply {
                duration = 500L + Random.nextLong(0, 200)
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                startDelay = Random.nextLong(0, 200)
                addUpdateListener {
                    val newHeight = it.animatedValue as Int
                    bar.layoutParams.height = newHeight
                    bar.requestLayout()
                }
            }
            animators.add(animator)
            animator.start()
        }
    }

    private fun stopFakeWaveAnimation() {
        animators.forEach { it.cancel() }
        animators.clear()
    }

    private fun startMicBreathAnimation() {
        if (micAnimator != null) return
        val micView = binding.llWaveGroup.getChildAt(0)
        micAnimator = ObjectAnimator.ofPropertyValuesHolder(
            micView,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.2f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.2f, 1f)
        ).apply {
            duration = 1600
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun stopMicBreathAnimation() {
        micAnimator?.cancel()
        micAnimator = null
    }

    private fun Int.dp(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}


fun Int.dp(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

