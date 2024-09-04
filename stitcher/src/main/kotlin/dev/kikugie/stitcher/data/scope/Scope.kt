package dev.kikugie.stitcher.data.scope

import dev.kikugie.stitcher.data.token.MarkerType
import dev.kikugie.stitcher.data.scope.ScopeType.*
import dev.kikugie.stitcher.data.block.Block
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents the id of scope in the Stitcher program.
 *
 * Type defines how the parser should close the scope and how the transformer should modify it.
 * - [CLOSED] - Comment ends with `{`. Parser will look for a comment starting with `}` to close the scope.
 * - [LINE] - Comment doesn't have a key character for the ending.
 * Parser will assign the next [Block] to the scope and close it.
 * Transformer will apply the change to the next logical line.
 * (i.e. the current line or the next one if the current one has nothing after the condition)
 * - [WORD] - Comment ends with `>>`.
 * Parser will assign the next [Block] to the scope and close it.
 * Transformer will apply the change to the next uninterrupted string.
 * (i.e. a string that ends with a whitespace or a line break)
 */
@Serializable
enum class ScopeType(val id: String) {
    CLOSED("{"),
    LINE(""),
    WORD(">>")
}

/**
 * Represents a scope in a Stitcher program.
 *
 * Scopes allow the parser and transformer to handle nested expressions.
 * This can be seen in other languages as the following:
 * ```kt
 *  if (bool1) { // a closed scope
 *      // blocks of the scope
 *      func1()
 *      func2()
 *      if (bool2) // a scope for the next line
 *          func3() // block of this scope and its end
 *  } // scope ends here
 * ```
 * Stitcher expressions start with a unique identifier, which determines the syntax.
 * Closed scopes must end with a comment of the same identifier.
 *
 * (i.e., the following is not allowed)
 * ```
 *  //$ id {
 *  //? if expr {
 *  //$}
 *  //?}
 *  ```
 *  It could be useful to have swap tokens dynamically change the condition,
 *  but that would make parsing and transforming the code much more difficult.
 *
 * @property type The id of the id associated with the scope.
 * @property enclosure The id of scope in the Stitcher program.
 * @property blocks The list of blocks contained in the scope.
 *
 * @constructor Creates a new Scope instance with the specified id and enclosure.
 *
 * @see ScopeType
 */
@Serializable
data class Scope(
    // Not serialized because only used for parsing the tree
    @Transient var type: MarkerType? = null,
    @Transient var enclosure: ScopeType = CLOSED,
    val blocks: MutableList<Block> = mutableListOf()
) : MutableList<Block> by blocks {
    fun <T> accept(visitor: Visitor<T>): T = visitor.visitScope(this)

    interface Visitor<T> {
        fun visitScope(it: Scope): T
    }
}