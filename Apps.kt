package com.glass.launcher

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class AppEntry(
    val label: String,
    val pkg: String,
    val component: ComponentName,
    val image: ImageBitmap,
    val isBanner: Boolean,
)

fun loadApps(pm: PackageManager, self: String): List<AppEntry> {
    fun query(cat: String) =
        pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(cat), 0)

    return (query(Intent.CATEGORY_LEANBACK_LAUNCHER) + query(Intent.CATEGORY_LAUNCHER))
        .distinctBy { it.activityInfo.packageName }
        .filter { it.activityInfo.packageName != self }
        .map { ri ->
            val ai = ri.activityInfo
            val banner = ai.loadBanner(pm)
            val drawable = banner ?: ai.loadIcon(pm)
            val bmp = if (banner != null) drawable.toBitmap(320, 180) else drawable.toBitmap(128, 128)
            AppEntry(
                label = ri.loadLabel(pm).toString(),
                pkg = ai.packageName,
                component = ComponentName(ai.packageName, ai.name),
                image = bmp.asImageBitmap(),
                isBanner = banner != null,
            )
        }
        .sortedBy { it.label.lowercase() }
}
