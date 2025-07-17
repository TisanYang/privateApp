package com.tisan.share.acty

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayout
import com.tisan.location.R
import com.tisan.location.databinding.ActivityMainBinding
import com.tisan.share.base.BaseActivity
import com.tisan.share.fra.FunctionFragment
import com.tisan.share.fra.ManagerFragment
import com.tisan.share.fra.MyFragment
import com.tisan.share.fra.PrivateFilesFragment
import com.tisan.share.vm.MainViewModel

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    override val viewModelClass = MainViewModel::class.java

    private var currentFragmentIndex = -1

    override fun inflateBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    private val fragments by lazy {
        listOf(
            PrivateFilesFragment(),
            FunctionFragment(),
            //ManagerFragment(),
            MyFragment()
        )
    }

    override fun initViews() {

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(0, 0, 0, bottom)
            insets
        }

        // 默认切第一个
        switchFragment(0)

        viewModel.tabList.forEachIndexed { index, tabInfo ->
            val tab = binding.tabLayout.newTab()
            tab.customView = viewModel.createTabView(this, tabInfo, index == 0)
            binding.tabLayout.addTab(tab)
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.selectTab(tab.position)
                viewModel.updateTabSelected(
                    tab.customView ?: return,
                    viewModel.tabList[tab.position],
                    true,
                    this@MainActivity
                )
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                viewModel.updateTabSelected(
                    tab.customView ?: return,
                    viewModel.tabList[tab.position],
                    false,
                    this@MainActivity
                )
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun observeData() {
        viewModel.selectedTabIndex.observe(this) { index ->
            switchFragment(index)
        }
    }

    private fun switchFragment(index: Int) {
        if (index == currentFragmentIndex) return
        currentFragmentIndex = index

        val transaction = supportFragmentManager.beginTransaction()
        val fragmentTag = "tab_$index"

        val existing = supportFragmentManager.findFragmentByTag(fragmentTag)
        fragments.forEachIndexed { i, _ ->
            supportFragmentManager.findFragmentByTag("tab_$i")?.let {
                transaction.hide(it)
            }
        }

        if (existing != null) {
            transaction.show(existing)
        } else {
            transaction.add(R.id.fragmentContainer, fragments[index], fragmentTag)
        }

        transaction.commitAllowingStateLoss()
    }

}