package org.openedx.foundation.system

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource

/**
 * iOS implementation of ResourceManager.
 *
 * For Compose Resources we delegate to the multiplatform getString() (suspending).
 * runBlocking is used to bridge to the synchronous interface — same pattern as
 * AndroidResourceManager. Compose Resources reads from the framework bundle, so
 * no main-thread dependency.
 *
 * For Android Int IDs (interface artifact) we return a debug placeholder — those
 * code paths shouldn't fire on iOS once the migration completes.
 */
class IosResourceManager : ResourceManager {

    override fun getString(id: Int): String = "[res:$id]"

    override fun getString(id: Int, vararg formatArgs: Any): String =
        "[res:$id args=${formatArgs.joinToString()}]"

    override fun getString(resource: StringResource): String = runBlocking {
        org.jetbrains.compose.resources.getString(resource)
    }

    override fun getString(resource: StringResource, vararg formatArgs: Any): String = runBlocking {
        org.jetbrains.compose.resources.getString(resource, *formatArgs)
    }
}
