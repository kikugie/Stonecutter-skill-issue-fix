package dev.kikugie.stitcher.process.util

import java.util.NoSuchElementException

class LookaroundIterator<T>(private val iterator: Iterator<T>) : Iterator<T> {
    constructor(iterable: Iterable<T>) : this(iterable.iterator())

    var current: T? = null
        private set
    var peek: T? = null
        private set
    var prev: T? = null
        private set

    init {
        if (iterator.hasNext()) {
            peek = iterator.next()
        }
    }

    override fun hasNext() = iterator.hasNext()

    override fun next(): T {
        prev = current
        current = peek
        peek = if (iterator.hasNext()) iterator.next() else null

        return current ?: throw NoSuchElementException("No more elements present.")
    }
}