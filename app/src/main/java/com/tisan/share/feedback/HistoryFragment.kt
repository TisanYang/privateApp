package com.tisan.share.feedback

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.kj.infinite.databinding.FraHistoryBinding
import com.tisan.share.base.BaseFragment


class HistoryFragment : BaseFragment<FraHistoryBinding,HistoryViewModel>() {

    private lateinit var adapter: FeedbackAdapter
    override val viewModelClass = HistoryViewModel::class.java

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FraHistoryBinding = FraHistoryBinding.inflate(layoutInflater)

    override fun initData() {
        adapter = FeedbackAdapter { record ->
            // 点击追问按钮
            // 例如通过 Navigation 返回并传参
            Toast.makeText(requireContext(), "准备追问：${record.question}", Toast.LENGTH_SHORT).show()
            (activity as? FeedbackActivity)?.switchToFeedbackFragment(record.question)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.records.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

}
