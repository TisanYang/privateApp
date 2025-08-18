package com.tisan.share.base

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.kj.infinite.R

class TitleView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val backIcon: ImageView
    private val titleText: TextView
    private val rightText: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.title_view, this, true)
        backIcon = findViewById(R.id.iv_back)
        titleText = findViewById(R.id.tv_title)
        rightText = findViewById(R.id.tv_right)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleView)
        val title = typedArray.getString(R.styleable.TitleView_titleText) ?: ""
        val centered = typedArray.getBoolean(R.styleable.TitleView_titleCentered, false)
        val rightTxt = typedArray.getString(R.styleable.TitleView_rightText)
        val rightIconRes = typedArray.getResourceId(R.styleable.TitleView_rightIcon, 0)
        val showBackBtn = typedArray.getBoolean(R.styleable.TitleView_showBack, true)
        typedArray.recycle()

        setTitle(title, centered)
        if (!rightTxt.isNullOrEmpty()) {
            setRightText(rightTxt)
        } else if (rightIconRes != 0) {
            setRightIcon(rightIconRes)
        } else {
            rightText.visibility = View.GONE
        }

        backIcon.visibility = if (showBackBtn) View.VISIBLE else View.GONE
        if (showBackBtn) {
            backIcon.setOnClickListener {
                (context as? Activity)?.finish()
            }
        }
    }

    fun setTitle(text: String, center: Boolean = false) {
        titleText.text = text
        val params = titleText.layoutParams as LayoutParams
        params.addRule(RelativeLayout.CENTER_VERTICAL)
        if (center) {
            params.addRule(RelativeLayout.CENTER_IN_PARENT)
            params.removeRule(RelativeLayout.END_OF) // 防止与返回按钮冲突
        } else {
            params.addRule(RelativeLayout.END_OF, R.id.iv_back)
        }
        titleText.layoutParams = params
    }

    fun setRightText(text: String, onClick: (() -> Unit)? = null) {
        rightText.visibility = View.VISIBLE
        rightText.text = text
        rightText.setOnClickListener { onClick?.invoke() }
    }

    fun setRightIcon(@DrawableRes iconRes: Int, onClick: (() -> Unit)? = null) {
        rightText.visibility = View.VISIBLE
        rightText.text = ""
        rightText.setBackgroundResource(iconRes)
        rightText.setOnClickListener { onClick?.invoke() }
    }

    fun setOnBackClick(onClick: () -> Unit) {
        backIcon.setOnClickListener { onClick() }
    }

    fun setOnRightClickListener(listener: () -> Unit) {
        rightText.setOnClickListener { listener() }
    }

    fun hideRightText() {
        rightText.visibility = View.GONE
    }

    fun showRightText() {
        rightText.visibility = View.VISIBLE
    }


}
