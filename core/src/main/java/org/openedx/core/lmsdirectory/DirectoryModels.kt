package org.openedx.core.lmsdirectory

/**
 * What the app knows about a platform: enough to list it, and enough to become it.
 */

data class LmsSummary(
    val id: String,
    val title: String,
    val shortDescription: String,
    val baseUrl: String,
    val logoUrl: String?,
    val accentColor: String?,
)

/**
 * Full record for one platform, fetched when the learner picks it. Carries the
 * per-LMS OAuth client id and feedback email needed to actually sign in against it,
 * plus branding (logo, accent) — the catalog summary alone can't log you in.
 */
data class LmsDetail(
    val id: String,
    val title: String,
    val shortDescription: String = "",
    val baseUrl: String,
    val logoUrl: String?,
    val accentColor: String?,
    val oauthClientId: String?,
    val feedbackEmail: String?,
    val loginBackgroundUrl: String?,
    /** When true, the app opens the pre-login course Discovery screen instead of sign-in. */
    val preLoginDiscovery: Boolean = false,
)
