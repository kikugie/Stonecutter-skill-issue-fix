package dev.kikugie.stonecutter.intellij.util

inline fun <T, R> memoize(crossinline func: (T) -> R) = object : Memoizer<T, R>(mutableMapOf()) {
    override fun supply(t: T): R = func(t)
}

abstract class Memoizer<T, R>(private val cache: MutableMap<T, R>) : (T) -> R {
    fun clear() = cache.clear()
    abstract fun supply(t: T): R
    override fun invoke(t: T): R = cache[t] ?: run { supply(t).also { cache[t] = it } }
}