package org.openedx.foundation.system

import org.jetbrains.compose.resources.StringResource

interface ResourceManager {
    fun getString(id: Int): String
    fun getString(id: Int, vararg formatArgs: Any): String
    fun getString(resource: StringResource): String
    fun getString(resource: StringResource, vararg formatArgs: Any): String
}
