package org.openedx.foundation.extension

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import com.google.gson.Gson
import java.io.Serializable

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

fun <T> T.objectToString(): String {
    return Gson().toJson(this)
}

@JvmName("objectToStringFromValue")
fun <T> objectToString(value: T): String {
    return Gson().toJson(value)
}

inline fun <reified T> String.stringToObject(): T {
    return Gson().fromJson(this, genericType<T>())
}

@JvmName("stringToObjectFromJson")
inline fun <reified T> stringToObject(json: String): T? {
    return try {
        Gson().fromJson(json, genericType<T>())
    } catch (_: Exception) {
        null
    }
}
