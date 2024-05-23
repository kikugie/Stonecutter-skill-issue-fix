package dev.kikugie.version.predicate

import dev.kikugie.version.impl.Version
import dev.kikugie.version.operator.VersionComparisonOperator

interface VersionPredicate : (Version) -> Boolean {
    interface PredicateTerm {
        val operator: VersionComparisonOperator
        val reference: Version
    }
}