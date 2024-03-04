package dev.kikugie.stitcher.parser

import dev.kikugie.stitcher.token.NULL
import dev.kikugie.stitcher.token.TokenType
import kotlinx.serialization.Serializable

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

@Serializable
class RootScope(val version: Int) : Scope(NULL, ScopeType.CLOSED)