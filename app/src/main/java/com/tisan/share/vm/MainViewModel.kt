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
        TabInfo("文件", R.drawable.file_unsel, R.drawable.file_sel),
        TabInfo("拍摄", R.drawable.take_unsel, R.drawable.take_sel),
        TabInfo("收藏", R.drawable.love_unsel, R.drawable.love_sel),
        TabInfo("我的", R.drawable.my_unsel, R.drawable.my_sel)
    )

    fun createTabView(context: Context, info: TabInfo, selected: Boolean): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_tab_custom, null)
        val icon = view.findViewById<ImageView>(R.id.tabIcon)
        val text = view.findViewById<TextView>(R.id.tabText)

        icon.setImageResource(if (selected) info.iconSelected else info.iconNormal)
        text.text = info.title

        val colorRes = if (selected) R.color.text_sel else R.color.text_secondary
        text.setTextColor(ContextCompat.getColor(context, colorRes))

        return view
    }

    fun updateTabSelected(view: View, info: TabInfo, selected: Boolean, context: Context) {
        val icon = view.findViewById<ImageView>(R.id.tabIcon)
        val text = view.findViewById<TextView>(R.id.tabText)

        icon.setImageResource(if (selected) info.iconSelected else info.iconNormal)

        val colorRes = if (selected) R.color.text_sel else R.color.text_secondary
        text.setTextColor(ContextCompat.getColor(context, colorRes))
    }
}