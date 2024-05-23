import dev.kikugie.stitcher.process.recognizer.CharRecognizer
import dev.kikugie.stitcher.process.recognizer.IdentifierRecognizer
import dev.kikugie.stitcher.process.recognizer.PredicateRecognizer
import dev.kikugie.stitcher.process.recognizer.StringRecognizer
import dev.kikugie.stitcher.data.StitcherTokenType

@Suppress("MemberVisibilityCanBePrivate")
object Syntax {
    val SCOPE_OPEN = CharRecognizer('{', StitcherTokenType.SCOPE_OPEN)
    val SCOPE_CLOSE = CharRecognizer('}', StitcherTokenType.SCOPE_CLOSE)
    val EXPECT_WORD = StringRecognizer(">>", StitcherTokenType.EXPECT_WORD)

    val GROUP_OPEN = CharRecognizer('(', StitcherTokenType.GROUP_OPEN)
    val GROUP_CLOSE = CharRecognizer(')', StitcherTokenType.GROUP_CLOSE)

    val ASSIGN = CharRecognizer(':', StitcherTokenType.ASSIGN)
    val NEGATE = CharRecognizer('!', StitcherTokenType.NEGATE)
    val AND = StringRecognizer("&&", StitcherTokenType.AND)
    val OR = StringRecognizer("||", StitcherTokenType.OR)
    val IF = StringRecognizer("if", StitcherTokenType.IF)
    val ELSE = StringRecognizer("else", StitcherTokenType.ELSE)
    val ELIF = StringRecognizer("elif", StitcherTokenType.ELIF)
    val IDENTIFIER = IdentifierRecognizer(StitcherTokenType.IDENTIFIER)
    val PREDICATE = PredicateRecognizer(StitcherTokenType.PREDICATE)

    val ALL = listOf(
        GROUP_OPEN, GROUP_CLOSE, SCOPE_OPEN, SCOPE_CLOSE, ASSIGN, NEGATE, IF, ELSE, ELIF, AND, OR, EXPECT_WORD, IDENTIFIER, PREDICATE
    )
}