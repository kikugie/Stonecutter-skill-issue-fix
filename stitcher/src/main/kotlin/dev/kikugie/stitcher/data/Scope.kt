package dev.kikugie.stitcher.data

import dev.kikugie.stitcher.data.ScopeType.*
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents the id of scope in the Stitcher program.
 *
 * Type defines how the parser should close the scope and how the transformer should modify it.
 * - [CLOSED] - comment ends with `{`. Parser will look for a comment starting with `}` to close the scope.
 * - [LINE] - comment doesn't have a key character for the ending.
 * Parser will assign the next [Block] to the scope and close it.
 * Transformer will apply the change to the next logical line.
 * (i.e. the current line or the next one if the current one has nothing after the condition)
 * - [WORD] - comment ends with `>>`.
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
 * (i.e. the following is not allowed)
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
    @Transient
    val type: TokenType = NullType,
    @Transient
    val enclosure: ScopeType = CLOSED
) : MutableCollection<Block> {
    private var blocks: MutableList<Block> = mutableListOf()
    override val size get() = blocks.size
    override fun clear() = blocks.clear()
    override fun isEmpty() = blocks.isEmpty()
    override fun containsAll(elements: Collection<Block>) = blocks.containsAll(elements)
    override fun contains(element: Block) = blocks.contains(element)
    override fun addAll(elements: Collection<Block>) = blocks.addAll(elements)
    override fun add(element: Block) = blocks.add(element)
    override fun retainAll(elements: Collection<Block>) = blocks.retainAll(elements)
    override fun removeAll(elements: Collection<Block>) = blocks.removeAll(elements)
    override fun remove(element: Block) = blocks.remove(element)
    override fun iterator() = blocks.iterator()

    fun <T> accept(visitor: Visitor<T>): T = visitor.visitScope(this)

    interface Visitor<T> {
        fun visitScope(it: Scope): T
    }
}