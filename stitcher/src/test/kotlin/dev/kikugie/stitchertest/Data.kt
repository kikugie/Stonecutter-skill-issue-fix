package dev.kikugie.stitchertest

import dev.kikugie.stitcher.data.ContentType.*
import dev.kikugie.stitcher.data.MarkerType
import dev.kikugie.stitcher.data.StitcherTokenType.*

val SCANNER_TESTS = buildList {
    add("slash comment", "// comment") {
        token("//", COMMENT_START)
        token(" comment", COMMENT)
    }
    add("hash comment", "# comment") {
        token("#", COMMENT_START)
        token(" comment", COMMENT)
    }
    add(
        "doc comment",
        """
            /**
            * doc comment
            */
            """.trimIndent()
    ) {
        token("/*", COMMENT_START)
        token("*\n * doc comment\n ", COMMENT)
        token("*/", COMMENT_END)
    }
    add("multi comment", "/* comment */") {
        token("/*", COMMENT_START)
        token(" comment ", COMMENT)
        token("*/", COMMENT_END)
    }
    add("open comment", "/* comment") {
        token("/*", COMMENT_START)
        token(" comment", COMMENT)
    }
    add("nested slash-slash comment", "// comment // comment") {
        token("//", COMMENT_START)
        token(" comment // comment", COMMENT)
    }
    add("nested slash-hash comment", "// comment # comment") {
        token("//", COMMENT_START)
        token(" comment # comment", COMMENT)
    }
    add("nested multi-multi comment", "/* comm /* comment */ ent */") {
        token("/*", COMMENT_START)
        token(" comm /* comment ", COMMENT)
        token("*/", COMMENT_END)
        token(" ent */", CONTENT)
    }
    add("nested slash-multi comment", "// comm /* comment */ ent") {
        token("//", COMMENT_START)
        token(" comm /* comment */ ent", COMMENT)
    }
    add("nested multi-slash comment", "/* comm // ent */") {
        token("/*", COMMENT_START)
        token(" comm // ent ", COMMENT)
        token("*/", COMMENT_END)
    }
    add("quote in comment", "// \"cool\" comment") {
        token("//", COMMENT_START)
        token(" \"cool\" comment", COMMENT)
    }
    add("comment in quote", "\"nice // quote\"") {
        token("\"nice // quote\"", CONTENT)
    }
    add("invalid comment quote", "/* comm \"ent */ wtf\"") {
        token("/*", COMMENT_START)
        token(" comm \"ent ", COMMENT)
        token("*/", COMMENT_END)
        token(" wtf\"", CONTENT)
    }
    add("invalid quote comment", "\"quote /* comm\" ent*/") {
        token("\"quote /* comm\" ent*/", CONTENT)
    }
    add("single quote in doubles", "\" quote '\"") {
        token("\" quote '\"", CONTENT)
    }
    add("double quote in singles", "' quote \"'") {
        token("' quote \"'", CONTENT)
    }
    add("double quote in doc", "\"\"\" still \"quote\" \"\"\"") {
        token("\"\"\" still \"quote\" \"\"\"", CONTENT)
    }
    add("escaped quote", "\" quote \\\" \"") {
        token("\" quote \\\" \"", CONTENT)
    }
}

val LEXER_TESTS = buildList {
    add("base tokens", "/*? { } ( ) ! || && if else an expression */") {
        token("/*", COMMENT_START)
        token("?", MarkerType.CONDITION)
        token("{", SCOPE_OPEN)
        token("}", SCOPE_CLOSE)
        token("(", GROUP_OPEN)
        token(")", GROUP_CLOSE)
        token("!", NEGATE)
        token("||", OR)
        token("&&", AND)
        token("if", IF)
        token("else", ELSE)
        token("*/", COMMENT_END)
    }
}

val PARSER_TESTS = buildList {
    tuple(
        "flat tree",
        """
        // this is a function!
        func()
        """.trimIndent(),
        """
        blocks:
        - !<dev.kikugie.stitcher.data.CommentBlock>
          start:
            value: "//"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
          content: !<dev.kikugie.stitcher.data.Literal>
            type:
              value: " this is a function!"
              type: !<dev.kikugie.stitcher.type.Comment> "COMMENT"
          end:
            value: "\n"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
        - !<dev.kikugie.stitcher.data.ContentBlock>
          type:
            value: "func()"
            type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
        version: 1
        """.trimIndent()
    )
    tuple(
        "swap type",
        """
        //$ type {
        func()
        //$}
        """.trimIndent(),
        """
        blocks:
        - !<dev.kikugie.stitcher.data.CommentBlock>
          start:
            value: "//"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
          content: !<dev.kikugie.stitcher.data.Swap>
            identifier:
              value: "type"
              type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
          end:
            value: "\n"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
          scope:
            type: !<dev.kikugie.stitcher.type.StitcherToken> "SWAP"
            blocks:
            - !<dev.kikugie.stitcher.data.ContentBlock>
              type:
                value: "func()\n"
                type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
            - !<dev.kikugie.stitcher.data.CommentBlock>
              start:
                value: "//"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.data.Swap>
                extension: true
              end:
                value: ""
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
        version: 1
        """.trimIndent()
    )
    tuple(
        "condition type",
        """
        /*? if bool {*/
        func()
        /*?} else {*/
        // func2()
        /*?}
        """.trimIndent(),
        """
        blocks:
        - !<dev.kikugie.stitcher.data.CommentBlock>
          start:
            value: "/*"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
          content: !<dev.kikugie.stitcher.data.Condition>
            sugar:
            - value: "if"
              type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
            condition: !<dev.kikugie.stitcher.data.Literal>
              type:
                value: "bool"
                type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
          end:
            value: "*/"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
          scope:
            type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
            blocks:
            - !<dev.kikugie.stitcher.data.ContentBlock>
              type:
                value: "\nfunc()\n"
                type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
        - !<dev.kikugie.stitcher.data.CommentBlock>
          start:
            value: "/*"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
          content: !<dev.kikugie.stitcher.data.Condition>
            sugar:
            - value: "else"
              type: !<dev.kikugie.stitcher.type.StitcherToken> "ELSE"
            extension: true
          end:
            value: "*/"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
          scope:
            type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
            blocks:
            - !<dev.kikugie.stitcher.data.ContentBlock>
              type:
                value: "\n"
                type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
            - !<dev.kikugie.stitcher.data.CommentBlock>
              start:
                value: "//"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.data.Literal>
                type:
                  value: " func2()"
                  type: !<dev.kikugie.stitcher.type.Comment> "COMMENT"
              end:
                value: "\n"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
        - !<dev.kikugie.stitcher.data.CommentBlock>
          start:
            value: "/*"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
          content: !<dev.kikugie.stitcher.data.Condition>
            extension: true
          end:
            value: ""
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
        version: 1
        """.trimIndent()
    )
    tuple(
        "condition scope",
        """
        //? if bool1 && !bool2 {
        func1()
        // func2()
        //?} else if (bool1) || (bool2)
        /*func3()*/
        """.trimIndent(),
        """
        blocks:
        - !<dev.kikugie.stitcher.data.CommentBlock>
          start:
            value: "//"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
          content: !<dev.kikugie.stitcher.data.Condition>
            sugar:
            - value: "if"
              type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
            condition: !<dev.kikugie.stitcher.data.Binary>
              left: !<dev.kikugie.stitcher.data.Literal>
                type:
                  value: "bool1"
                  type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
              operator:
                value: "&&"
                type: !<dev.kikugie.stitcher.type.StitcherToken> "AND"
              right: !<dev.kikugie.stitcher.data.Unary>
                operator:
                  value: "!"
                  type: !<dev.kikugie.stitcher.type.StitcherToken> "NEGATE"
                target: !<dev.kikugie.stitcher.data.Literal>
                  type:
                    value: "bool2"
                    type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
          end:
            value: "\n"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
          scope:
            type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
            blocks:
            - !<dev.kikugie.stitcher.data.ContentBlock>
              type:
                value: "func1()\n"
                type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
            - !<dev.kikugie.stitcher.data.CommentBlock>
              start:
                value: "//"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.data.Literal>
                type:
                  value: " func2()"
                  type: !<dev.kikugie.stitcher.type.Comment> "COMMENT"
              end:
                value: "\n"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
        - !<dev.kikugie.stitcher.data.CommentBlock>
          start:
            value: "//"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
          content: !<dev.kikugie.stitcher.data.Condition>
            sugar:
            - value: "else"
              type: !<dev.kikugie.stitcher.type.StitcherToken> "ELSE"
            - value: "if"
              type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
            condition: !<dev.kikugie.stitcher.data.Binary>
              left: !<dev.kikugie.stitcher.data.Group>
                content: !<dev.kikugie.stitcher.data.Literal>
                  type:
                    value: "bool1"
                    type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
              operator:
                value: "||"
                type: !<dev.kikugie.stitcher.type.StitcherToken> "OR"
              right: !<dev.kikugie.stitcher.data.Group>
                content: !<dev.kikugie.stitcher.data.Literal>
                  type:
                    value: "bool2"
                    type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
            extension: true
          end:
            value: "\n"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
          scope:
            type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
            enclosure: "LINE"
        - !<dev.kikugie.stitcher.data.CommentBlock>
          start:
            value: "/*"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
          content: !<dev.kikugie.stitcher.data.Literal>
            type:
              value: "func3()"
              type: !<dev.kikugie.stitcher.type.Comment> "COMMENT"
          end:
            value: "*/"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
        version: 1
        """.trimIndent()
    )
    tuple(
        "nested scopes",
        """
        //? if bool1 {
        //? if bool2 {
        //? if bool3
        func()
        //?}
        //?}
        """.trimIndent(),
        """
        blocks:
        - !<dev.kikugie.stitcher.data.CommentBlock>
          start:
            value: "//"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
          content: !<dev.kikugie.stitcher.data.Condition>
            sugar:
            - value: "if"
              type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
            condition: !<dev.kikugie.stitcher.data.Literal>
              type:
                value: "bool1"
                type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
          end:
            value: "\n"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
          scope:
            type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
            blocks:
            - !<dev.kikugie.stitcher.data.CommentBlock>
              start:
                value: "//"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.data.Condition>
                sugar:
                - value: "if"
                  type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
                condition: !<dev.kikugie.stitcher.data.Literal>
                  type:
                    value: "bool2"
                    type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
              end:
                value: "\n"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
              scope:
                type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
                blocks:
                - !<dev.kikugie.stitcher.data.CommentBlock>
                  start:
                    value: "//"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
                  content: !<dev.kikugie.stitcher.data.Condition>
                    sugar:
                    - value: "if"
                      type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
                    condition: !<dev.kikugie.stitcher.data.Literal>
                      type:
                        value: "bool3"
                        type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
                  end:
                    value: "\n"
                    type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
                  scope:
                    type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
                    enclosure: "LINE"
                    blocks:
                    - !<dev.kikugie.stitcher.data.ContentBlock>
                      type:
                        value: "func()\n"
                        type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
            - !<dev.kikugie.stitcher.data.CommentBlock>
              start:
                value: "//"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
              content: !<dev.kikugie.stitcher.data.Condition>
                extension: true
              end:
                value: "\n"
                type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
        - !<dev.kikugie.stitcher.data.CommentBlock>
          start:
            value: "//"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
          content: !<dev.kikugie.stitcher.data.Condition>
            extension: true
          end:
            value: ""
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
        version: 1
        """.trimIndent()
    )
    tuple(
        "expect word",
        "/*? if >=1.20 >>*/ word1 word2",
        """
        blocks:
        - !<dev.kikugie.stitcher.data.CommentBlock>
          start:
            value: "/*"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_START"
          content: !<dev.kikugie.stitcher.data.Condition>
            sugar:
            - value: "if"
              type: !<dev.kikugie.stitcher.type.StitcherToken> "IF"
            condition: !<dev.kikugie.stitcher.data.Literal>
              type:
                value: ">=1.20"
                type: !<dev.kikugie.stitcher.type.StitcherToken> "EXPRESSION"
          end:
            value: "*/"
            type: !<dev.kikugie.stitcher.type.Comment> "COMMENT_END"
          scope:
            type: !<dev.kikugie.stitcher.type.StitcherToken> "CONDITION"
            enclosure: "WORD"
            blocks:
            - !<dev.kikugie.stitcher.data.ContentBlock>
              type:
                value: " word1 word2"
                type: !<dev.kikugie.stitcher.type.Comment> "CONTENT"
        version: 1
        """.trimIndent()
    )
}