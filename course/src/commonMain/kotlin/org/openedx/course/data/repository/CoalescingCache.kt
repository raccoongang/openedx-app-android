package org.openedx.course.data.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A cache with request coalescing support.
 *
 * When multiple callers request the same data simultaneously,
 * only one fetch operation is performed and all callers receive the same result.
 *
 * @param K the type of cache keys
 * @param V the type of cached values
 * @param fetch the suspend function to fetch data for a given key
 * @param persist optional callback invoked after successful fetch (e.g., to save to database)
 */
class CoalescingCache<K, V>(
    private val fetch: suspend (K) -> V,
    private val persist: (suspend (K, V) -> Unit)? = null
) {
    private val cache = mutableMapOf<K, V>()
    private val pending = mutableMapOf<K, CompletableDeferred<V>>()
    private val mutex = Mutex()

    fun getCached(key: K): V? = cache[key]

    fun setCached(key: K, value: V) {
        cache[key] = value
    }

    fun clear() {
        cache.clear()
    }

    suspend fun getOrFetch(key: K, forceRefresh: Boolean = false): V {
        if (!forceRefresh) {
            cache[key]?.let { return it }
        }

        val (deferred, isOwner) = mutex.withLock {
            val existing = pending[key]
            if (existing != null) {
                existing to false
            } else {
                val d = CompletableDeferred<V>()
                pending[key] = d
                d to true
            }
        }

        return if (isOwner) {
            try {
                val result = fetch(key)
                cache[key] = result
                persist?.invoke(key, result)
                deferred.complete(result)
                result
            } catch (e: Exception) {
                deferred.completeExceptionally(e)
                throw e
            } finally {
                mutex.withLock { pending.remove(key) }
            }
        } else {
            deferred.await()
        }
    }
}
