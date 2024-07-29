package dev.kikugie.stitcher.eval

import dev.kikugie.stitcher.data.block.Block
import dev.kikugie.stitcher.data.block.CodeBlock
import dev.kikugie.stitcher.data.block.CommentBlock
import dev.kikugie.stitcher.data.block.ContentBlock
import dev.kikugie.stitcher.data.component.*
import dev.kikugie.stitcher.data.token.Token

/**
 * Checks if a given Stitcher component doesn't contain any meaningful content.
 *
 * @see Component
 */
object EmptyChecker : Component.Visitor<Boolean>, Block.Visitor<Boolean> {
    override fun visitEmpty(it: Empty): Boolean = true
    override fun visitLiteral(it: Literal): Boolean = it.token.isBlank()
    override fun visitGroup(it: Group): Boolean = it.content.isEmpty()
    override fun visitUnary(it: Unary): Boolean = it.operator.isBlank() && it.target.isEmpty()
    override fun visitBinary(it: Binary): Boolean =
        it.operator.isBlank() && it.left.isEmpty() && it.right.isEmpty()

    override fun visitAssignment(it: Assignment): Boolean = it.target.isBlank() && it.predicates.all(Token::isBlank)
    override fun visitCondition(it: Condition): Boolean = it.sugar.all(Token::isBlank) && it.condition.accept(this)
    override fun visitSwap(it: Swap): Boolean = it.identifier.isBlank()
    override fun visitDefinition(it: Definition): Boolean = it.component.isEmpty()

    // Code and comments have surrounding symbols anyway
    override fun visitCode(it: CodeBlock): Boolean = false
    override fun visitComment(it: CommentBlock): Boolean = false
    override fun visitContent(it: ContentBlock): Boolean = it.content.isBlank()
}