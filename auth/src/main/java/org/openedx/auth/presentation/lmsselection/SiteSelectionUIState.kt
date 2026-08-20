package org.openedx.auth.presentation.lmsselection

import org.openedx.core.lmsdirectory.LmsSummary

data class SiteSelectionUIState(
    /** The publisher's own name, shown above the list. Blank when they gave none. */
    val providerName: String = "",
    val platforms: List<LmsSummary> = emptyList(),
    val catalog: CatalogState = CatalogState.Loading,

    /**
     * Every image the directory will ask for. The whole list arrives at once, so
     * these are known before a platform is picked; the screen warms them so the
     * branded sign-in does not assemble itself in front of the learner.
     */
    val imageReferences: List<String> = emptyList(),
)

sealed interface CatalogState {
    data object Loading : CatalogState
    data object Loaded : CatalogState
    data object Empty : CatalogState
    data class Error(val message: String) : CatalogState
}
