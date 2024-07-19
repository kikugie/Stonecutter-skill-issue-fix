package dev.kikugie.stitcher.parser

object Messages {
    const val WRONG_TOKEN = "Unexpected token"
    const val WRONG_SWAP = "Unknown swap identifier"
    const val DUPLICATE_SWAP = "Duplicate swap identifier"
    const val CLOSER_SWAP = "Swap identifiers are not allowed in closer blocks"
    const val NO_END = "Expected end of comment"
    const val EMPTY_BODY = "Scope specifiers are not allowed in closer blocks"
    const val WRONG_CLOSER = "Invalid comment closer"
    const val WRONG_SUGAR = "Invalid condition statement"
    const val NO_CONDITION = "Must have a condition statement"
    const val PLS_NO_CONDITION = "Must not have a condition statement"
    const val NO_EXTENSION = "Expected to follow '}' to extend the condition"
    const val PLS_NO_EXTENSION = "Expected 'else' or 'elif' to follow the extension"
    const val WRONG_DEP = "Unknown dependency"
    const val NO_PREDICATES = "Missing predicates"
    const val WRONG_CONST = "Unknown constant"
    const val NO_GROUP_END = "Expected ')' to close the group"
}