package org.openedx.core.lmsdirectory

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A directory read from a single JSON document — hosted or shipped in the app —
 * has to behave exactly like one read from a live service, and keep behaving
 * that way with no network at all. That is the promise the document makes.
 */
class DocumentLmsDirectorySourceTest {

    private val document = """
        {
          "format": "v1",
          "provider": { "name": "Northwind", "tagline": "Five campuses, one app" },
          "include": [
            {
              "name": "Alpha",
              "description": "Alpha",
              "url": "https://alpha.example.edu",
              "logo": "https://cdn.example.com/alpha.png",
              "accent_color": "#112233",
              "api": {
                "host_url": "https://alpha.example.edu",
                "oauth_client_id": "alpha-client",
                "feedback_email": "support@example.edu"
              },
              "feature_flags": { "pre_login_discovery": true },
              "theme": { "login_background": "alpha-bg.png" }
            },
            {
              "name": "Beta",
              "description": "Beta",
              "url": "https://beta.example.edu",
              "logo": "beta-logo.png",
              "api": {
                "host_url": "https://beta.example.edu",
                "oauth_client_id": "beta-client",
                "feedback_email": ""
              },
              "feature_flags": { "pre_login_discovery": false }
            }
          ]
        }
    """.trimIndent()

    private fun source(payload: String = document, onLoad: () -> Unit = {}) =
        DocumentLmsDirectorySource(
            loader = {
                onLoad()
                payload
            }
        )

    @Test
    fun `every platform is listed in document order`() = runTest {
        assertEquals(listOf("Alpha", "Beta"), source().platforms().map { it.title })
    }

    @Test
    fun `detail comes from the same copy without loading again`() = runTest {
        var loads = 0
        val source = source(onLoad = { loads++ })
        source.platforms()
        val detail = source.detail("1")

        assertEquals("Beta", detail.title)
        assertEquals("beta-client", detail.oauthClientId)
        // Read once and kept: the picker, the theming and the prefetch all work
        // from one copy rather than fetching it three times.
        assertEquals(1, loads)
    }

    @Test
    fun `two platforms may share an address without merging`() = runTest {
        // Identifying a platform by its URL merges these two: the list draws one
        // and opening it gives the other one's settings.
        val payload = """
            {
              "format": "v1",
              "include": [
                { "name": "Alpha", "description": "Alpha", "url": "https://shared.example.edu",
                  "accent_color": "#111111" },
                { "name": "Beta", "description": "Beta", "url": "https://shared.example.edu",
                  "accent_color": "#222222" }
              ]
            }
        """.trimIndent()
        val source = source(payload = payload)

        val items = source.platforms()
        assertEquals(listOf("Alpha", "Beta"), items.map { it.title })
        assertEquals(2, items.map { it.id }.toSet().size)

        val second = source.detail(items[1].id)
        assertEquals("Beta", second.title)
        assertEquals("#222222", second.accentColor)
    }

    @Test
    fun `an unknown id is an error rather than a silent empty result`() = runTest {
        val source = source()
        try {
            source.detail("9")
            throw AssertionError("Expected a failure for an unknown id")
        } catch (e: NoSuchElementException) {
            assertTrue(e.message!!.contains("9"))
        }
    }

    @Test
    fun `the provider name comes from the document`() = runTest {
        assertEquals("Northwind", source().providerName())
    }

    @Test
    fun `image references cover logos and sign-in backgrounds`() = runTest {
        val refs = source().imageReferences()
        assertTrue(refs.contains("https://cdn.example.com/alpha.png"))
        assertTrue(refs.contains("alpha-bg.png"))
        assertTrue(refs.contains("beta-logo.png"))
    }

    @Test
    fun `a minimal hand-written document is accepted`() = runTest {
        // The smallest document a person could reasonably write. The same file
        // has to work on iOS, so anything omitted here must have a default on
        // both platforms — not just on this one, where Gson is forgiving.
        val minimal = """
            {
              "format": "v1",
              "include": [
                {
                  "name": "Alpha",
                  "description": "Alpha",
                  "url": "https://alpha.example.edu"
                }
              ]
            }
        """.trimIndent()

        val detail = source(payload = minimal).detail("0")

        assertEquals("Alpha", detail.title)
        // No "api" block at all: the platform is served from the address the
        // learner picked, and the app signs in with its own OAuth client.
        assertEquals("https://alpha.example.edu", detail.baseUrl)
        assertNull(detail.oauthClientId)
    }

    @Test
    fun `a platform need not carry its own oauth client`() = runTest {
        // A multi-instance app carries one client id of its own, which each
        // backend registers. A directory that names none per platform is the
        // normal case, and must not stop the file being read.
        val payload = """
            {
              "format": "v1",
              "include": [
                {
                  "name": "Alpha",
                  "description": "Alpha",
                  "url": "https://alpha.example.edu",
                  "api": { "host_url": "https://api.alpha.example.edu" }
                }
              ]
            }
        """.trimIndent()

        val detail = source(payload = payload).detail("0")

        assertEquals("https://api.alpha.example.edu", detail.baseUrl)
        assertNull(detail.oauthClientId)
        assertNull(detail.feedbackEmail)
    }

    @Test
    fun `a document that cannot be parsed fails instead of looking empty`() = runTest {
        try {
            source(payload = "not json at all").platforms()
            throw AssertionError("Expected a parse failure")
        } catch (e: Exception) {
            assertTrue(e !is AssertionError)
        }
    }
}
