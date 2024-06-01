package dev.kikugie.semver

enum class VersionComparisonOperator(val literal: String) : (SemanticVersion, SemanticVersion) -> Boolean {
    EQUAL("=") {
        override fun invoke(p1: SemanticVersion, p2: SemanticVersion): Boolean = p1.compareTo(p2) == 0
    },
    LESS("<") {
        override fun invoke(p1: SemanticVersion, p2: SemanticVersion): Boolean = p1 < p2
    },
    LESS_EQUAL("<=") {
        override fun invoke(p1: SemanticVersion, p2: SemanticVersion): Boolean = p1 <= p2
    },
    GREATER(">") {
        override fun invoke(p1: SemanticVersion, p2: SemanticVersion): Boolean = p1 > p2
    },
    GREATER_EQUAL(">=") {
        override fun invoke(p1: SemanticVersion, p2: SemanticVersion): Boolean = p1 >= p2
    },
    SAME_TO_NEXT_MINOR("~") {
        override fun invoke(p1: SemanticVersion, p2: SemanticVersion): Boolean = p1 >= p2 && p1[0] == p2[0] && p1[1] == p2[1]
    },
    SAME_TO_NEXT_MAJOR("^") {
        override fun invoke(p1: SemanticVersion, p2: SemanticVersion): Boolean = p1 >= p2 && p1[0] == p2[0]
    };

    companion object {
        val MATCHER = entries.associateBy { it.literal }

        fun CharSequence.operatorLength(offset: Int = 0): Int = when (this[offset]) {
            '=', '~', '^' -> 1
            '>', '<' -> if (getOrNull(offset + 1) == '=') 2 else 1
            else -> 0
        }
    }
}