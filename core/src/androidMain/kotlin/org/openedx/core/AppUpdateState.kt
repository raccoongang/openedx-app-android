package org.openedx.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun AppUpdateState.openPlayMarket(context: Context) {
    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "market://details?id=${context.packageName}".toUri()
            )
        )
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()
            )
        )
    }
}
