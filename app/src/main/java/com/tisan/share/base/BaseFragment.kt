package com.tisan.share.base

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.kj.infinite.R
import com.tisan.share.dia.FolderLoadingDialog

abstract class BaseFragment<Binding : ViewBinding, VM : BaseViewModel> : Fragment() {

    lateinit var binding: Binding
    lateinit var viewModel: VM

    private var loadingDialog: Dialog? = null

    // 子类提供对应的 ViewModel class
    protected abstract val viewModelClass: Class<VM>

    // 子类提供 Binding inflate 方法
    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //makeStatusBarTransparent()
        binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[viewModelClass]
        initView()
        initData()
        observeData()
        initListeners()
        observeUiState()
    }

    open fun initView() {}
    open fun initData(){}
    open fun observeData() {}
    open fun initListeners(){}

    private fun observeUiState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> showLoading()
                is UiState.Success<*> -> {
                    dismissLoading()
                    showToast("操作成功")
                }
                is UiState.Failure -> {
                    dismissLoading()
                    showToast(state.msg)
                }
                else -> {}
            }
        }
    }

    protected fun <T> LiveData<UiState<T>>.observeUiState(
        owner: LifecycleOwner = viewLifecycleOwner,
        onSuccess: (T?) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        observe(owner) { state ->
            when (state) {
                is UiState.Loading -> showLoading()
                is UiState.Success -> {
                    dismissLoading()
                    showToast("操作成功")
                    onSuccess(state.data)
                }
                is UiState.Failure -> {
                    dismissLoading()
                    showToast(state.msg)
                    onFailure(state.msg)
                }
                else -> {}
            }
        }
    }


    private fun showLoading() {
        if (loadingDialog == null) {
            loadingDialog = FolderLoadingDialog(requireContext()).apply {
                //setContentView(R.layout.dialog_loading)
                setCancelable(false)
            }
        }
        loadingDialog?.show()
    }

    private fun dismissLoading() {
        loadingDialog?.dismiss()
    }
    protected fun log(msg: String) {
        Log.d(this::class.java.simpleName, msg)
    }

    protected fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }


    private fun makeStatusBarTransparent() {
        activity?.let {
            WindowCompat.setDecorFitsSystemWindows(it.window, false)
            WindowInsetsControllerCompat(it.window, it.window.decorView).isAppearanceLightStatusBars = true
            it.window.statusBarColor = Color.TRANSPARENT
        }
    }
}

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T?) : UiState<T>()
    data class Failure(val msg: String) : UiState<Nothing>()
}

