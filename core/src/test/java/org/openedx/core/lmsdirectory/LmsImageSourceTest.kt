package org.openedx.core.lmsdirectory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * One field decides whether an image is downloaded or read out of the app. The
 * rule is simple enough to state in a sentence, which is why it needs tests: an
 * operator editing the document by hand will lean on it.
 */
class LmsImageSourceTest {

    @Test
    fun `web addresses are passed through untouched`() {
        assertEquals(
            "https://cdn.example.com/logo.png",
            LmsImageSource.model("https://cdn.example.com/logo.png")
        )
        assertEquals(
            "http://cdn.example.com/logo.png",
            LmsImageSource.model("http://cdn.example.com/logo.png")
        )
    }

    @Test
    fun `anything else becomes an asset in the app`() {
        assertEquals("file:///android_asset/acme.png", LmsImageSource.model("acme.png"))
        assertEquals("file:///android_asset/logos/acme.png", LmsImageSource.model("logos/acme.png"))
        // A leading slash is a habit from the hosted form; it must not produce a
        // double slash that fails to resolve.
        assertEquals("file:///android_asset/acme.png", LmsImageSource.model("/acme.png"))
    }

    @Test
    fun `surrounding whitespace does not change the answer`() {
        assertEquals(
            "https://cdn.example.com/logo.png",
            LmsImageSource.model("  https://cdn.example.com/logo.png  ")
        )
        assertEquals("file:///android_asset/acme.png", LmsImageSource.model(" acme.png "))
    }

    @Test
    fun `empty and missing values produce nothing`() {
        assertNull(LmsImageSource.model(null))
        assertNull(LmsImageSource.model(""))
        assertNull(LmsImageSource.model("   "))
    }

    @Test
    fun `remote is decided by scheme, not by looking like a URL`() {
        assertTrue(LmsImageSource.isRemote("https://a.test/x.png"))
        assertTrue(LmsImageSource.isRemote("HTTPS://a.test/x.png"))
        assertFalse(LmsImageSource.isRemote("a.test/x.png"))
        assertFalse(LmsImageSource.isRemote("x.png"))
    }
}
