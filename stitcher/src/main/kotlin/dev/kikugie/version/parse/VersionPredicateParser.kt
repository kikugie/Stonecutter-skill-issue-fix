package dev.kikugie.version.parse

import dev.kikugie.version.exception.VersionParsingException
import dev.kikugie.version.impl.SemanticVersion
import dev.kikugie.version.impl.Version
import dev.kikugie.version.operator.VersionComparisonOperator
import dev.kikugie.version.predicate.VersionPredicate

class VersionPredicateParser(private val versionParser: Parser<Version>) : Parser<VersionPredicate> {
    private val OPERATORS = VersionComparisonOperator.entries

    override fun parse(predicate: CharSequence): VersionPredicate {
        val predicates = mutableListOf<SingleVersionPredicate>()
        for (it in predicate.split(' ').map(String::trim)) {
            var str = it
            if (str.isEmpty() || str == "*") continue
            var operator = VersionComparisonOperator.EQUAL
            for (op in OPERATORS) if (str.startsWith(op.literal)) {
                operator = op
                str = str.substring(op.literal.length)
                break
            }
            val version = versionParser.parse(str)
            if (version !is SemanticVersion && !operator.minInclusive && !operator.maxInclusive)
                throw VersionParsingException("Invalid predicate $predicate")
            predicates += SingleVersionPredicate(operator, version)
        }
        return if (predicates.isEmpty()) AnyVersionPredicate
        else MultiVersionPredicate(predicates)
    }

    object AnyVersionPredicate : VersionPredicate {
        override fun invoke(p1: Version): Boolean = true
    }

    class SingleVersionPredicate(
        override val operator: VersionComparisonOperator,
        override val reference: Version,
    ) : VersionPredicate, VersionPredicate.PredicateTerm {
        override fun invoke(p1: Version): Boolean = operator(p1, reference)
        override fun toString(): String = "${operator.literal}$reference"
        override fun hashCode(): Int = operator.ordinal * 31 + reference.hashCode()
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SingleVersionPredicate

            if (operator != other.operator) return false
            if (reference != other.reference) return false

            return true
        }
    }

    class MultiVersionPredicate(val predicates: Collection<SingleVersionPredicate>) : VersionPredicate {
        override fun invoke(p1: Version): Boolean = predicates.all { it(p1) }
        override fun hashCode(): Int = predicates.hashCode()
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MultiVersionPredicate

            return predicates == other.predicates
        }
    }
}