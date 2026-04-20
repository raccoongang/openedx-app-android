package org.openedx.foundation.extension

fun <T> List<T>.indexOfFirstFromIndex(startIndex: Int, predicate: (T) -> Boolean): Int {
    // Note: startIndex is EXCLUSIVE — matches the Android foundation library's bytecode
    // which uses `if (index > startIndex)` before checking predicate.
    for (index in (startIndex + 1) until this.size) {
        if (predicate(this[index])) return index
    }
    return -1
}

fun <T> ArrayList<T>.clearAndAddAll(collection: Collection<T>): ArrayList<T> {
    clear()
    addAll(collection)
    return this
}

fun <T> List<T>.clearAndAddAll(collection: Collection<T>): List<T> {
    return collection.toList()
}

fun <T> List<T>.isNotEmptyThenLet(block: (List<T>) -> Unit) {
    if (this.isNotEmpty()) {
        block(this)
    }
}
