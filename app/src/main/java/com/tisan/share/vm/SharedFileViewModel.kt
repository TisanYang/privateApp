package com.tisan.share.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tisan.share.data.FileRepository
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.datdabean.FileModuleItem
import com.tisan.share.datdabean.ModuleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SharedFileViewModel @Inject constructor() : ViewModel() {

    private val _modules = MutableStateFlow<List<FileModuleItem>>(emptyList())
    val modules: StateFlow<List<FileModuleItem>> = _modules.asStateFlow()

    private val _refreshSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshSignal = _refreshSignal.asSharedFlow()

    init {
        // 监听底层仓库的刷新事件，转发给 UI 页面
        viewModelScope.launch {
            FileRepository.refreshEvent.collect {
                _refreshSignal.emit(Unit)
            }
        }
    }

    private var currentFilterType: ModuleType? = null


//    fun loadModulesIfNeeded(context: Context) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val modules = FileRepository.loadAllModules(context)
//            withContext(Dispatchers.Main) {
//                _modules.value = modules
//            }
//        }
//    }


    // 按模块类型筛选
    fun applyFilter(type: ModuleType?) {
        currentFilterType = type
        val filtered = if (type == null) {
            FileRepository.getModulesByType(null)
        } else {
            FileRepository.getModulesByType(type)
        }
        _modules.value = filtered
    }

    // 新增模块
    fun addModule(module: FileModuleItem) {
        FileRepository.addModule(module)
        applyFilter(currentFilterType)
    }

    // 删除模块
    fun removeModule(type: ModuleType) {
        FileRepository.removeModuleByType(type)
        applyFilter(currentFilterType)
    }

    // 更新模块
    fun updateModule(module: FileModuleItem) {
        FileRepository.updateModule(module)
        applyFilter(currentFilterType)
    }

    // 增加模块内文件
    fun addFileToModule(type: ModuleType, file: EncryptedFileItem) {
        FileRepository.addFileToModule(type, file)
        applyFilter(currentFilterType)
    }

    // 删除模块内文件
    fun removeFileFromModule(type: ModuleType, filePath: String) {
        FileRepository.removeFileFromModule(type, filePath)
        applyFilter(currentFilterType)
    }

    // 清除所有数据
    fun clearAll() {
        FileRepository.clearCache()
        _modules.value = emptyList()
    }
}
