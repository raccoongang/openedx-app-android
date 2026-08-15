package org.openedx.core.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Which source a build reads its platform list from is decided entirely by the
 * config file, so the rule has to be exactly what the comments in that file say.
 */
class LMSDirectoryConfigTest {

    @Test
    fun `a json address is a document, anything else is a service`() {
        assertEquals(
            LMSDirectoryConfig.Source.Document("https://example.com/directory.json"),
            LMSDirectoryConfig(enabled = true, directoryUrl = "https://example.com/directory.json").source
        )
        assertEquals(
            LMSDirectoryConfig.Source.Service("https://example.com"),
            LMSDirectoryConfig(enabled = true, directoryUrl = "https://example.com").source
        )
    }

    @Test
    fun `a query string does not hide the json suffix`() {
        assertEquals(
            LMSDirectoryConfig.Source.Document("https://example.com/d.json?v=2"),
            LMSDirectoryConfig(enabled = true, directoryUrl = "https://example.com/d.json?v=2").source
        )
    }

    @Test
    fun `a bundled file wins over an address`() {
        // A build shipping its own copy has opted out of the network; quietly
        // preferring a remote list would undo that.
        assertEquals(
            LMSDirectoryConfig.Source.BundledDocument("lms_directory.json"),
            LMSDirectoryConfig(
                enabled = true,
                directoryUrl = "https://example.com/directory.json",
                directoryFile = "lms_directory.json",
            ).source
        )
    }

    @Test
    fun `nothing configured means no source and nothing reachable`() {
        assertNull(LMSDirectoryConfig(enabled = true).source)
        assertFalse(LMSDirectoryConfig(enabled = true).isReachable)
        // Off is off, whatever else is filled in.
        assertNull(
            LMSDirectoryConfig(enabled = false, directoryUrl = "https://example.com/d.json").source
        )
        assertFalse(
            LMSDirectoryConfig(enabled = false, directoryUrl = "https://example.com/d.json").isReachable
        )
    }

    @Test
    fun `a bundled file alone is enough to be reachable`() {
        assertTrue(LMSDirectoryConfig(enabled = true, directoryFile = "lms_directory.json").isReachable)
    }
}
