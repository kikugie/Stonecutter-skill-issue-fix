package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.type.NULL
import dev.kikugie.stitcher.type.TokenType
import kotlinx.serialization.Serializable

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
 * (i.e. the following is not allowed)
 * ```
 *  //$ token {
 *  //? if expr {
 *  //$}
 *  //?}
 *  ```
 *  It could be useful to have swap tokens dynamically change the condition,
 *  but that would make parsing and transforming the code much more difficult.
 *
 * @property type The type of the token associated with the scope.
 * @property enclosure The type of scope in the Stitcher program.
 * @property blocks The list of blocks contained in the scope.
 *
 * @constructor Creates a new Scope instance with the specified type and enclosure.
 *
 * @see ScopeType
 */
@Serializable
open class Scope(
    val type: TokenType,
    val enclosure: ScopeType,
) {
    val blocks: MutableList<Block> = mutableListOf()

    fun add(block: Block) {
        blocks.add(block)
    }

    fun remove(block: Block) {
        blocks.remove(block)
    }

    fun replace(source: Block, block: Block) {
        blocks[blocks.indexOf(source)] = block
    }

    fun find(predicate: (Block) -> Boolean): Block? {
        return blocks.find(predicate)
    }
}

/**
 * Represents the root scope in a Stitcher program.
 *
 * The root scope is a special type of scope that is used to define the highest-level scope in a Stitcher program.
 * It serves as the entry point for parsing and transforming the program.
 *
 * Scope type and enclosure don't matter for this one.
 * [ScopeType.CLOSED] is used, so the parser doesn't quit its job immediately.
 *
 * @property version The version of the Stitcher program. Used to validate cached ASTs.
 * @constructor Creates a new RootScope instance with the specified version.
 * @see Scope
 * @see ScopeType
 * @see Parser.VERSION
 */
@Serializable
class RootScope(val version: Int) : Scope(NULL, ScopeType.CLOSED)