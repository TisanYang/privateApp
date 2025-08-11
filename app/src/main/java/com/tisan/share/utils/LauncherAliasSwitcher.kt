package com.tisan.share.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object LauncherAliasSwitcher {
    fun switchTo(context: Context, simpleAlias: String) {
        val pkg = context.packageName
        val all = listOf("$pkg.AliasA", "$pkg.AliasB", "$pkg.AliasC")
        val target = "$pkg.$simpleAlias"
        val pm = context.packageManager

        // 1) enable target
        pm.setComponentEnabledSetting(
            ComponentName(pkg, target),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        // 2) disable others
        for (name in all) if (name != target) {
            pm.setComponentEnabledSetting(
                ComponentName(pkg, name),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}


