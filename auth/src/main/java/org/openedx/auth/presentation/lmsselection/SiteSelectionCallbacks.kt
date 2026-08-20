package org.openedx.auth.presentation.lmsselection

import org.openedx.core.lmsdirectory.LmsSummary

/** UI callbacks for [SiteSelectionScreen]. */
class SiteSelectionCallbacks(
    val onPlatformSelected: (LmsSummary) -> Unit,
    val onRetry: () -> Unit,
)
