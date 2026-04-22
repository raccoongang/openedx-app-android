package org.openedx.core.data.storage

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.uint8_tVar

/**
 * Keychain-backed string store for sensitive values (auth tokens).
 *
 * Items use kSecClassGenericPassword with kSecAttrAccessibleAfterFirstUnlock
 * so background tasks can still read them after device reboot. Each value
 * is addressed by a unique `account` under the shared `service`.
 */
@OptIn(ExperimentalForeignApi::class)
internal class KeychainStore(private val service: String = DEFAULT_SERVICE) {

    fun get(key: String): String? = memScoped {
        val query = buildQuery(key)
        CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        CFRelease(query)

        if (status != errSecSuccess) return@memScoped null
        val rawRef = result.value ?: return@memScoped null
        val dataRef: CFDataRef = rawRef.reinterpret()
        val length = CFDataGetLength(dataRef).toInt()
        if (length <= 0) {
            CFRelease(rawRef)
            return@memScoped ""
        }
        val bytes = CFDataGetBytePtr(dataRef) ?: run {
            CFRelease(rawRef)
            return@memScoped null
        }
        val buffer = bytes.readBytes(length)
        CFRelease(rawRef)
        buffer.decodeToString()
    }

    fun set(key: String, value: String) {
        // Delete first — SecItemAdd returns errSecDuplicateItem otherwise.
        remove(key)

        val bytes = value.encodeToByteArray()
        val data: CFDataRef = bytes.usePinned { pinned ->
            val ptr = if (bytes.isEmpty()) null else pinned.addressOf(0).reinterpret<uint8_tVar>()
            CFDataCreate(kCFAllocatorDefault, ptr, bytes.size.convert())
        } ?: return

        val query = buildQuery(key)
        CFDictionarySetValue(query, kSecValueData, data)
        val status = SecItemAdd(query, null)
        CFRelease(query)
        CFRelease(data)
        if (status != errSecSuccess) {
            println("[KeychainStore] SecItemAdd failed for $key with OSStatus=$status")
        }
    }

    fun remove(key: String) {
        val query = buildQuery(key)
        SecItemDelete(query)
        CFRelease(query)
    }

    private fun buildQuery(key: String): CFMutableDictionaryRef {
        val dict = CFDictionaryCreateMutable(
            kCFAllocatorDefault, 0,
            kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr,
        )!!
        CFDictionarySetValue(dict, kSecClass, kSecClassGenericPassword)
        val serviceRef = cfString(service)
        val accountRef = cfString(key)
        CFDictionarySetValue(dict, kSecAttrService, serviceRef)
        CFDictionarySetValue(dict, kSecAttrAccount, accountRef)
        CFDictionarySetValue(dict, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlock)
        // Values are retained by the dictionary — release our local refs.
        CFRelease(serviceRef)
        CFRelease(accountRef)
        return dict
    }

    private fun cfString(s: String): CFStringRef =
        CFStringCreateWithCString(kCFAllocatorDefault, s, kCFStringEncodingUTF8)!!

    companion object {
        private const val DEFAULT_SERVICE = "org.openedx.secure"
    }
}
