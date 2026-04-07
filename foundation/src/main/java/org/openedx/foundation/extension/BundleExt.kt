package org.openedx.foundation.extension

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.Serializable

@PublishedApi
internal val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    coerceInputValues = true
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(key)
    }
}

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializable(key) as? T
    }
}

inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayList(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArrayList(key)
    }
}

inline fun <reified T> T.objectToString(): String {
    return json.encodeToString(this)
}

@JvmName("objectToStringFromValue")
inline fun <reified T> objectToString(value: T): String {
    return json.encodeToString(value)
}

inline fun <reified T> String.stringToObject(): T {
    return json.decodeFromString(this)
}

@JvmName("stringToObjectFromJson")
inline fun <reified T> stringToObject(jsonString: String): T? {
    return try {
        json.decodeFromString(jsonString)
    } catch (_: Exception) {
        null
    }
}
