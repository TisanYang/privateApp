package com.tisan.share.datdabean

import androidx.annotation.DrawableRes

data class TabInfo(
    val title: String,
    @DrawableRes val iconNormal: Int,
    @DrawableRes val iconSelected: Int
)