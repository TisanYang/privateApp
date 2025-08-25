package com.tisan.share.feedback

import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.kj.infinite.databinding.FragmentFeedbackBinding
import com.tisan.share.base.BaseFragment
import com.tisan.share.net.responsebean.CommentResponse

class FeedbackFragment : BaseFragment<FragmentFeedbackBinding, FeedbackViewModel>() {

    override val viewModelClass = FeedbackViewModel::class.java
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFeedbackBinding = FragmentFeedbackBinding.inflate(layoutInflater)

    override fun initView() {
        binding.btnSubmit.setOnClickListener {
            val feedbackText = binding.etFeedback.text.toString().trim()
            if (feedbackText.isEmpty()) {
                Toast.makeText(requireContext(), "请输入反馈内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 调用 ViewModel 提交
            viewModel.submitFeedbackInO(feedbackText, requireContext())
//            viewModel.launchWithUiState<CommentResponse>(
//                onSuccess = { data ->
//                    binding.etFeedback.setText("反馈成功：${data?.id}")
//                },
//                onFailure = { msg ->
//                    // 如果你想自定义失败处理（可选）
//                    Toast.makeText(context, "提交失败：$msg", Toast.LENGTH_SHORT).show()
//                }
//            ) {
//                viewModel.submitFeedback(feedbackText, requireContext())
//            }
        }

        view?.viewTreeObserver?.addOnGlobalLayoutListener {
            val r = Rect()
            requireView().getWindowVisibleDisplayFrame(r)
            val screenHeight = requireView().rootView.height
            val keypadHeight = screenHeight - r.bottom
            val isKeyboardShown = keypadHeight > screenHeight * 0.15

            val translation = if (isKeyboardShown) -keypadHeight.toFloat() else 0f
            binding.btnSubmit.animate().translationY(translation).setDuration(200).start()
        }

    }

    override fun initListeners() {
        binding.etFeedback.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val maxLen = 200
                s?.let {
                    if (it.length > maxLen) {
                        it.delete(maxLen, it.length) // 超过部分自动删掉
                    }
                    // 这里你可以更新剩余字数提示，比如：
                    val remaining = maxLen - it.length
                    binding.tvCharCount.text = "还可以输入 $remaining 字"
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

    }

    override fun observeData() {

        viewModel.submitResultInO.observeUiState(
            onSuccess = {
                binding.etFeedback.setText("反馈成功：${it?.id}")
            },
            onFailure = {
                showToast("id")
            }
        )
    }
}
