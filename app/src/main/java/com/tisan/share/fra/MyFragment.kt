package com.tisan.share.fra

import android.view.LayoutInflater
import android.view.ViewGroup
import com.tisan.location.databinding.FraMyBinding
import com.tisan.share.base.BaseFragment
import com.tisan.share.vm.SimpleViewModel

class MyFragment : BaseFragment<FraMyBinding, SimpleViewModel>() {
    override val viewModelClass = SimpleViewModel::class.java

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FraMyBinding {
        return FraMyBinding.inflate(inflater, container, false)
    }

}