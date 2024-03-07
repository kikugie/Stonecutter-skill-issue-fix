package dev.kikugie.stitchertest

import dev.kikugie.stitcher.exception.StitcherSyntaxException
import dev.kikugie.stitcher.util.Buildable
import dev.kikugie.stitchertest.ParserTest.PairBuilder.Companion.pair
import dev.kikugie.stitchertest.util.parse
import dev.kikugie.stitchertest.util.yaml
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.*

object ParserTest {
    private class PairBuilder : Buildable<Pair<String, String>> {
        lateinit var left: String

        @Language("yaml")
        lateinit var right: String

        override fun build() = left to right

        companion object {
            fun pair(init: PairBuilder.() -> Unit) = PairBuilder().apply(init).build()
        }
    }

    val tests = buildMap {
        this["flat tree"] = pair {
            left = """
            // this is a function!
            func()
            """.trimIndent()
            right = """
            blocks:
            - !<dev.kikugie.stitcher.parser.CommentBlock>
              start:
                value: "//"
                range: "0..<2"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.parser.Literal>
                token:
                  value: " this is a function!"
                  range: "2..<22"
                  type: !<dev.kikugie.stitcher.type.Comment> "COMMENT"
              end:
                value: "\n"
                range: "22..<23"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
            - !<dev.kikugie.stitcher.parser.ContentBlock>
              token:
                value: "func()"
                range: "23..<29"
                type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
            version: 1
            """.trimIndent()
        }
        this["swap token"] = pair {
            left = """
                //$ token {
                func()
                //$}
            """.trimIndent()
            right = """
            blocks:
            - !<dev.kikugie.stitcher.parser.CommentBlock>
              start:
                value: "//"
                range: "0..<2"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.parser.Swap>
                identifier:
                  value: "token"
                  range: "4..<9"
                  type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
              end:
                value: "\n"
                range: "11..<12"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
              scope:
                type: !<dev.kikugie.stitcher.type.StitcherToken> "SWAP"
                blocks:
                - !<dev.kikugie.stitcher.parser.ContentBlock>
                  token:
                    value: "func()\n"
                    range: "12..<19"
                    type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
                - !<dev.kikugie.stitcher.parser.CommentBlock>
                  start:
                    value: "//"
                    range: "19..<21"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
                  content: !<dev.kikugie.stitcher.parser.Swap>
                    extension: true
                  end:
                    value: ""
                    range: "23..<-1"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
            version: 1
            """.trimIndent()
        }
        this["condition token"] = pair {
            left = """
            /*? if bool {*/
            func()
            /*?} else {*/
            // func2()
            /*?}
            """.trimIndent()
            right = """
            blocks:
            - !<dev.kikugie.stitcher.parser.CommentBlock>
              start:
                value: "/*"
                range: "0..<2"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.parser.Condition>
                sugar:
                - value: "if"
                  range: "4..<6"
                  type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
                condition: !<dev.kikugie.stitcher.parser.Literal>
                  token:
                    value: "bool"
                    range: "7..<11"
                    type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
              end:
                value: "*/"
                range: "13..<15"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
              scope:
                type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
                blocks:
                - !<dev.kikugie.stitcher.parser.ContentBlock>
                  token:
                    value: "\nfunc()\n"
                    range: "15..<23"
                    type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
            - !<dev.kikugie.stitcher.parser.CommentBlock>
              start:
                value: "/*"
                range: "23..<25"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.parser.Condition>
                sugar:
                - value: "else"
                  range: "28..<32"
                  type: !<dev.kikugie.stitcher.type.StitcherToken> "ELSE"
                extension: true
              end:
                value: "*/"
                range: "34..<36"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
              scope:
                type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
                blocks:
                - !<dev.kikugie.stitcher.parser.ContentBlock>
                  token:
                    value: "\n"
                    range: "36..<37"
                    type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
                - !<dev.kikugie.stitcher.parser.CommentBlock>
                  start:
                    value: "//"
                    range: "37..<39"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
                  content: !<dev.kikugie.stitcher.parser.Literal>
                    token:
                      value: " func2()"
                      range: "39..<47"
                      type: !<dev.kikugie.stitcher.type.Comment> "COMMENT"
                  end:
                    value: "\n"
                    range: "47..<48"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
                - !<dev.kikugie.stitcher.parser.CommentBlock>
                  start:
                    value: "/*"
                    range: "48..<50"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
                  content: !<dev.kikugie.stitcher.parser.Condition>
                    extension: true
                  end:
                    value: ""
                    range: "52..<-1"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
            version: 1
            """.trimIndent()
        }
        this["condition scope"] = pair {
            left = """
            //? if bool1 && !bool2 {
            func1()
            // func2()
            //?} else if (bool1) || (bool2)
            /*func3()*/
            """.trimIndent()
            right ="""
            blocks:
            - !<dev.kikugie.stitcher.parser.CommentBlock>
              start:
                value: "//"
                range: "0..<2"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.parser.Condition>
                sugar:
                - value: "if"
                  range: "4..<6"
                  type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
                condition: !<dev.kikugie.stitcher.parser.Binary>
                  left: !<dev.kikugie.stitcher.parser.Literal>
                    token:
                      value: "bool1"
                      range: "7..<12"
                      type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
                  operator:
                    value: "&&"
                    range: "13..<15"
                    type: !<dev.kikugie.stitcher.type.StitcherToken> "AND"
                  right: !<dev.kikugie.stitcher.parser.Unary>
                    operator:
                      value: "!"
                      range: "16..<17"
                      type: !<dev.kikugie.stitcher.type.StitcherToken> "NEGATE"
                    target: !<dev.kikugie.stitcher.parser.Literal>
                      token:
                        value: "bool2"
                        range: "17..<22"
                        type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
              end:
                value: "\n"
                range: "24..<25"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
              scope:
                type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
                blocks:
                - !<dev.kikugie.stitcher.parser.ContentBlock>
                  token:
                    value: "func1()\n"
                    range: "25..<33"
                    type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
                - !<dev.kikugie.stitcher.parser.CommentBlock>
                  start:
                    value: "//"
                    range: "33..<35"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
                  content: !<dev.kikugie.stitcher.parser.Literal>
                    token:
                      value: " func2()"
                      range: "35..<43"
                      type: !<dev.kikugie.stitcher.type.Comment> "COMMENT"
                  end:
                    value: "\n"
                    range: "43..<44"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
            - !<dev.kikugie.stitcher.parser.CommentBlock>
              start:
                value: "//"
                range: "44..<46"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.parser.Condition>
                sugar:
                - value: "else"
                  range: "49..<53"
                  type: !<dev.kikugie.stitcher.type.StitcherToken> "ELSE"
                - value: "if"
                  range: "54..<56"
                  type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
                condition: !<dev.kikugie.stitcher.parser.Binary>
                  left: !<dev.kikugie.stitcher.parser.Group>
                    content: !<dev.kikugie.stitcher.parser.Literal>
                      token:
                        value: "bool1"
                        range: "58..<63"
                        type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
                  operator:
                    value: "||"
                    range: "65..<67"
                    type: !<dev.kikugie.stitcher.type.StitcherToken> "OR"
                  right: !<dev.kikugie.stitcher.parser.Group>
                    content: !<dev.kikugie.stitcher.parser.Literal>
                      token:
                        value: "bool2"
                        range: "69..<74"
                        type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
                extension: true
              end:
                value: "\n"
                range: "75..<76"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
              scope:
                type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
                enclosure: "LINE"
            - !<dev.kikugie.stitcher.parser.CommentBlock>
              start:
                value: "/*"
                range: "76..<78"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.parser.Literal>
                token:
                  value: "func3()"
                  range: "78..<85"
                  type: !<dev.kikugie.stitcher.type.Comment> "COMMENT"
              end:
                value: "*/"
                range: "85..<87"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
            version: 1
            """.trimIndent()
        }
        this["nested scopes"] = pair {
            left = """
            //? if bool1 {
            //? if bool2 {
            //? if bool3
            func()
            //?}
            //?}
            """.trimIndent()
            right = """
            blocks:
            - !<dev.kikugie.stitcher.parser.CommentBlock>
              start:
                value: "//"
                range: "0..<2"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.parser.Condition>
                sugar:
                - value: "if"
                  range: "4..<6"
                  type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
                condition: !<dev.kikugie.stitcher.parser.Literal>
                  token:
                    value: "bool1"
                    range: "7..<12"
                    type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
              end:
                value: "\n"
                range: "14..<15"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
              scope:
                type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
                blocks:
                - !<dev.kikugie.stitcher.parser.CommentBlock>
                  start:
                    value: "//"
                    range: "15..<17"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
                  content: !<dev.kikugie.stitcher.parser.Condition>
                    sugar:
                    - value: "if"
                      range: "19..<21"
                      type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
                    condition: !<dev.kikugie.stitcher.parser.Literal>
                      token:
                        value: "bool2"
                        range: "22..<27"
                        type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
                  end:
                    value: "\n"
                    range: "29..<30"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
                  scope:
                    type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
                    blocks:
                    - !<dev.kikugie.stitcher.parser.CommentBlock>
                      start:
                        value: "//"
                        range: "30..<32"
                        type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
                      content: !<dev.kikugie.stitcher.parser.Condition>
                        sugar:
                        - value: "if"
                          range: "34..<36"
                          type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
                        condition: !<dev.kikugie.stitcher.parser.Literal>
                          token:
                            value: "bool3"
                            range: "37..<42"
                            type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
                      end:
                        value: "\n"
                        range: "42..<43"
                        type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
                      scope:
                        type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
                        enclosure: "LINE"
                    - !<dev.kikugie.stitcher.parser.ContentBlock>
                      token:
                        value: "func()\n"
                        range: "43..<50"
                        type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
                    - !<dev.kikugie.stitcher.parser.CommentBlock>
                      start:
                        value: "//"
                        range: "50..<52"
                        type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
                      content: !<dev.kikugie.stitcher.parser.Condition>
                        extension: true
                      end:
                        value: "\n"
                        range: "54..<55"
                        type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
                - !<dev.kikugie.stitcher.parser.CommentBlock>
                  start:
                    value: "//"
                    range: "55..<57"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
                  content: !<dev.kikugie.stitcher.parser.Condition>
                    extension: true
                  end:
                    value: ""
                    range: "59..<-1"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
            version: 1
            """.trimIndent()
        }
        this["expect word"] = pair {
            left = "/*? if >=1.20 >>*/ word1 word2"
            right = """
            blocks:
            - !<dev.kikugie.stitcher.parser.CommentBlock>
              start:
                value: "/*"
                range: "0..<2"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.parser.Condition>
                sugar:
                - value: "if"
                  range: "4..<6"
                  type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
                condition: !<dev.kikugie.stitcher.parser.Literal>
                  token:
                    value: ">=1.20"
                    range: "7..<13"
                    type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
              end:
                value: "*/"
                range: "16..<18"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
              scope:
                type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
                enclosure: "WORD"
            - !<dev.kikugie.stitcher.parser.ContentBlock>
              token:
                value: " word1 word2"
                range: "18..<30"
                type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
            version: 1
            """.trimIndent()
        }
    }

    fun check(input: String, expected: String) =
        Assertions.assertEquals(expected, input.parse().yaml())

    @TestFactory
    fun `test parser`() =
        tests.map { DynamicTest.dynamicTest(it.key) { check(it.value.first, it.value.second) } }

    @Test
    fun `cross-closing`() {
        val input = """
        //? bool {
        //$ token {
        //?}
        //$}
        """.trimIndent()
        assertThrows<StitcherSyntaxException>(input::parse)
    }
}