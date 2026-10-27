package com.example.quietfeed.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.edit

private const val KEY_PERMISSION_REQUESTED: String = "notification_permission_requested"
private const val PREFERENCES_NAME: String = "notification_permission"

@Composable
fun rememberNotificationPermissionRequest(): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    return remember(context, launcher) {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !context.hasNotificationPermission()) {
                val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                if (!preferences.getBoolean(KEY_PERMISSION_REQUESTED, false)) {
                    preferences.edit { putBoolean(KEY_PERMISSION_REQUESTED, true) }
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

private fun Context.hasNotificationPermission(): Boolean = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
