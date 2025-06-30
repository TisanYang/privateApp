package com.tisan.share.fra

import android.view.LayoutInflater
import android.view.ViewGroup
import com.tisan.location.databinding.FraFunctionBinding
import com.tisan.location.databinding.FraManagerBinding
import com.tisan.location.databinding.FraMyBinding
import com.tisan.share.base.BaseFragment
import com.tisan.share.vm.SimpleViewModel

class ManagerFragment : BaseFragment<FraManagerBinding, SimpleViewModel>() {
    override val viewModelClass = SimpleViewModel::class.java

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FraManagerBinding {
        return FraManagerBinding.inflate(inflater, container, false)
    }

}