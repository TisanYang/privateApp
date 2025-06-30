package com.tisan.share.vm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tisan.location.R
import com.tisan.share.base.BaseViewModel
import com.tisan.share.datdabean.TabInfo

object MainViewModel : BaseViewModel(){

    private val _selectedTabIndex = MutableLiveData(0)
    val selectedTabIndex: LiveData<Int> = _selectedTabIndex

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
    }

    val tabList = listOf(
        TabInfo("文件", R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_background),
        TabInfo("录音", R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_background),
        TabInfo("拍照", R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_background),
        TabInfo("更多", R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_background)
    )

    fun createTabView(context: Context, info: TabInfo, selected: Boolean): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_tab_custom, null)
        val icon = view.findViewById<ImageView>(R.id.tabIcon)
        val text = view.findViewById<TextView>(R.id.tabText)

        icon.setImageResource(if (selected) info.iconSelected else info.iconNormal)
        text.text = info.title

        val colorRes = if (selected) R.color.colorPrimary else R.color.tab_text_normal
        text.setTextColor(ContextCompat.getColor(context, colorRes))

        return view
    }

    fun updateTabSelected(view: View, info: TabInfo, selected: Boolean, context: Context) {
        val icon = view.findViewById<ImageView>(R.id.tabIcon)
        val text = view.findViewById<TextView>(R.id.tabText)

        icon.setImageResource(if (selected) info.iconSelected else info.iconNormal)

        val colorRes = if (selected) R.color.colorPrimary else R.color.tab_text_normal
        text.setTextColor(ContextCompat.getColor(context, colorRes))
    }
}