package org.openedx.foundation.system

interface ResourceManager {
    fun getString(id: Int): String
    fun getString(id: Int, vararg formatArgs: Any): String
}
