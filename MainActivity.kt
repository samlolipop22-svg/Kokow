package com.glass.launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var apps by mutableStateOf<List<AppEntry>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BackHandler {} // a launcher never "backs out"
            LauncherScreen(apps, onOpen = { openApp(it) }, onInfo = { showInfo(it) })
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            apps = withContext(Dispatchers.Default) { loadApps(packageManager, packageName) }
        }
    }

    private fun openApp(app: AppEntry) {
        val i = Intent(Intent.ACTION_MAIN)
            .setComponent(app.component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        runCatching { startActivity(i) }
    }

    private fun showInfo(app: AppEntry) {
        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.pkg}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { startActivity(i) }
    }
}
