package org.openedx.shared

/**
 * Platform interface for multiplatform code.
 * Each platform provides its own implementation.
 */
expect fun getPlatform(): Platform

interface Platform {
    val name: String
}

/**
 * Entry point for shared KMP code.
 * This module will eventually contain all shared business logic.
 */
object OpenEdXShared {
    fun greeting(): String = "OpenEdX running on ${getPlatform().name}"
}
