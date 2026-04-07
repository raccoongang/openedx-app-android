package org.openedx.foundation.extension

fun <T> List<T>.indexOfFirstFromIndex(startIndex: Int, predicate: (T) -> Boolean): Int {
    for (index in startIndex until this.size) {
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
