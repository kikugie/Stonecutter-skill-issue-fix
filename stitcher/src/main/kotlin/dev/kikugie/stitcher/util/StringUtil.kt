package dev.kikugie.stitcher.util

internal object StringUtil {
    fun CharSequence.countStart(vararg chars: Char): Int = countStart(0, *chars)
    fun CharSequence.countStart(offset: Int, vararg chars: Char): Int {
        var count = 0
        for (i in offset until length) {
            if (this[i] in chars) count++
            else return count
        }
        return count
    }

    inline fun CharSequence.countStart(predicate: (Char) -> Boolean): Int = countStart(0, predicate)
    inline fun CharSequence.countStart(offset: Int, predicate: (Char) -> Boolean): Int {
        var count = 0
        for (i in offset until length) {
            if (predicate(this[i])) count++
            else return count
        }
        return count
    }

    fun CharSequence.countEnd(vararg chars: Char): Int = countEnd(lastIndex, *chars)
    fun CharSequence.countEnd(offset: Int, vararg chars: Char): Int {
        var count = 0
        for (i in offset downTo 0) {
            if (this[i] in chars) count++
            else return count
        }
        return count
    }

    inline fun CharSequence.countEnd(predicate: (Char) -> Boolean): Int = countEnd(lastIndex, predicate)
    inline fun CharSequence.countEnd(offset: Int, predicate: (Char) -> Boolean): Int {
        var count = 0
        for (i in offset downTo 0) {
            if (predicate(this[i])) count++
            else return count
        }
        return count
    }
}