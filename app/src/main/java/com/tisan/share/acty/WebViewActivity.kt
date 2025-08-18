package com.tisan.share.acty


import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.kj.infinite.databinding.ActivityWebviewBinding
import com.tisan.share.base.BaseActivity
import com.tisan.share.vm.WebViewViewModel

class WebViewActivity :
    BaseActivity<ActivityWebviewBinding, WebViewViewModel>() {

    override val viewModelClass: Class<WebViewViewModel>
        get() = WebViewViewModel::class.java

    override fun inflateBinding() = ActivityWebviewBinding.inflate(layoutInflater)

    @SuppressLint("SetJavaScriptEnabled")
    override fun initViews() {
        // Enable JavaScript
        binding.webView.settings.javaScriptEnabled = true

        // WebViewClient for loading state
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                viewModel.setLoading(true)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                viewModel.setLoading(false)
            }
        }

        // WebChromeClient for progress & title
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                viewModel.setProgress(newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                val finalTitle = if (!title.isNullOrBlank()) title else DEFAULT_TITLE
                viewModel.setTitle(finalTitle)
            }
        }

        // Back button handling
        binding.titleView.setOnBackClick {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                finish()
            }
        }
    }

    override fun initData() {
        val url = intent.getStringExtra("url") ?: "https://www.baidu.com"
        viewModel.setUrl(url)
        binding.webView.loadUrl(url)
    }

    override fun observeData() {
        // Progress bar updates
        viewModel.progress.observe(this) { progress ->
            binding.progressBar.progress = progress
            binding.progressBar.visibility =
                if (progress in 1..99) View.VISIBLE else View.GONE
        }

        // TitleView updates
        viewModel.title.observe(this) { pageTitle ->
            binding.titleView.setTitle(pageTitle, true)
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val DEFAULT_TITLE = "Web Page"
    }
}
