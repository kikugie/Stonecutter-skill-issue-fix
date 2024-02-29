package dev.kikugie.stitcher.util

fun interface Buildable<out T> {
    fun build(): T

    companion object {
        fun <T> of(result: T): Buildable<T> = Buildable { result }
    }
}