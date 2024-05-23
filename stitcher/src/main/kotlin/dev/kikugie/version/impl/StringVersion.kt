package dev.kikugie.version.impl

data class StringVersion(private val content: String) : Version {
    override fun compareTo(other: Version): Int = other.toString().compareTo(content)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringVersion

        return content == other.content
    }

    override fun hashCode(): Int = content.hashCode()
}