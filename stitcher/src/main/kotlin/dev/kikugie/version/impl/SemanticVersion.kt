package dev.kikugie.version.impl

import java.util.*
import kotlin.math.max

open class SemanticVersion(
    val components: IntArray,
    val preModifier: String,
    val postModifier: String,
) : Version {
    private val friendlyName = buildString {
        append(components.joinToString("."))
        if (preModifier.isNotEmpty())
            append("-$preModifier")
        if (postModifier.isNotEmpty())
            append("+$postModifier")
    }

    operator fun get(index: Int): Int? = components.getOrNull(index)

    override fun compareTo(other: Version): Int = when (other) {
        is SemanticVersion -> compareToSemver(other)
        is MinecraftVersion ->
            if (other.semver == null) null
            else compareToSemver(other.semver!!)

        else -> null
    } ?: friendlyName.compareTo(other.toString())

    private fun compareToSemver(other: SemanticVersion): Int {
        val compareComponents = compareToComponents(other)
        return if (compareComponents != 0) compareComponents
        else compareToPreModifier(other)
    }

    private fun compareToComponents(other: SemanticVersion): Int {
        for (i in 0 until max(components.size, other.components.size)) {
            val first = components.getOrElse(i) { 0 }
            val second = other.components.getOrElse(i) { 0 }
            val compare = first.compareTo(second)
            if (compare != 0) return compare
        }
        return 0
    }

    private fun compareToPreModifier(other: SemanticVersion): Int {
        if (preModifier.isEmpty() && postModifier.isEmpty()) return 0
        if (preModifier.isEmpty() && other.preModifier.isNotEmpty()) return 1
        if (preModifier.isNotEmpty() && other.preModifier.isEmpty()) return -1

        val myTokenizer = StringTokenizer(preModifier, ".")
        val otherTokenizer = StringTokenizer(other.preModifier, ".")

        while (myTokenizer.hasMoreElements() || otherTokenizer.hasMoreElements()) {
            if (!myTokenizer.hasMoreElements()) return -1
            if (!otherTokenizer.hasMoreElements()) return 1

            val myPart = myTokenizer.nextToken()
            val otherPart = otherTokenizer.nextToken()

            val myPartInt = myPart.toIntOrNull()
            val otherPartInt = otherPart.toIntOrNull()

            if (myPartInt != null && otherPartInt != null) {
                val compare = myPartInt.compareTo(otherPartInt)
                if (compare != 0) return compare
            }
            if (myPartInt == null && otherPartInt != null)
                return 1
            if (myPartInt != null && otherPartInt == null)
                return -1
            val compare = myPart.compareTo(otherPart)
            if (compare != 0) return compare
        }
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SemanticVersion

        if (!components.contentEquals(other.components)) return false
        if (preModifier != other.preModifier) return false
        if (postModifier != other.postModifier) return false

        return true
    }

    override fun toString(): String = friendlyName

    override fun hashCode(): Int {
        var result = components.contentHashCode()
        result = 31 * result + preModifier.hashCode()
        result = 31 * result + postModifier.hashCode()
        return result
    }
}