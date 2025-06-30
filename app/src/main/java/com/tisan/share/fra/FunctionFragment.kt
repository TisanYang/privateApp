package com.tisan.share.fra

import android.view.LayoutInflater
import android.view.ViewGroup
import com.tisan.location.databinding.FraFunctionBinding
import com.tisan.location.databinding.FraMyBinding
import com.tisan.share.base.BaseFragment
import com.tisan.share.vm.SimpleViewModel

class FunctionFragment : BaseFragment<FraFunctionBinding, SimpleViewModel>() {
    override val viewModelClass = SimpleViewModel::class.java

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FraFunctionBinding {
        return FraFunctionBinding.inflate(inflater, container, false)
    }

}