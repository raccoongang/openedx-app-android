package org.openedx.shared.calendar

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberCalendarPermissionLauncher(
    onResult: (granted: Boolean) -> Unit,
): () -> Unit {
    val permissions = remember {
        arrayOf(
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CALENDAR,
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        onResult(result.values.all { it })
    }
    return remember(launcher) { { launcher.launch(permissions) } }
}
