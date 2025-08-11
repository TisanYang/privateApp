package com.tisan.share.acty

import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tisan.location.R
import com.tisan.location.databinding.ActivityModifyDisplayBinding
import com.tisan.share.base.BaseActivity
import com.tisan.share.dapter.DisplayOptionsAdapter
import com.tisan.share.data.DisplayOption
import com.tisan.share.utils.LauncherAliasSwitcher
import com.tisan.share.vm.SimpleViewModel

class ModifyDisplayActivity : BaseActivity<ActivityModifyDisplayBinding, SimpleViewModel>() {

    private val options = listOf(
        DisplayOption("AliasA", "隐私计算器", R.drawable.file_sel),
        DisplayOption("AliasB", "恭喜发财", R.drawable.love_sel),
        DisplayOption("AliasC", "好运连连", R.drawable.my_sel)
    )


    override val viewModelClass = SimpleViewModel::class.java

    override fun inflateBinding(): ActivityModifyDisplayBinding =
        ActivityModifyDisplayBinding.inflate(layoutInflater)

    override fun initViews() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this,2)
        recyclerView.adapter = DisplayOptionsAdapter(options) { option ->
            switchAppDisplay(option.aliasName)
        }
    }

    private fun switchAppDisplay(alias: String) {
        LauncherAliasSwitcher.switchTo(this, alias)
        Toast.makeText(this, "Switched to $alias", Toast.LENGTH_SHORT).show()
    }
}