package org.openedx.shared.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.EventKit.EKEntityType
import platform.EventKit.EKEventStore
import platform.Foundation.NSThread
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberCalendarPermissionLauncher(
    onResult: (granted: Boolean) -> Unit,
): () -> Unit {
    val store = remember { EKEventStore() }
    val currentOnResult = rememberUpdatedState(onResult)
    return remember(store) {
        {
            store.requestAccessToEntityType(EKEntityType.EKEntityTypeEvent) { granted, _ ->
                dispatchToMain {
                    currentOnResult.value(granted)
                }
            }
        }
    }
}

private fun dispatchToMain(block: () -> Unit) {
    if (NSThread.isMainThread) {
        block()
    } else {
        dispatch_async(dispatch_get_main_queue()) { block() }
    }
}
