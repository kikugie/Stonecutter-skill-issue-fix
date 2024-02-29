package dev.kikugie.stitchertest.scanner

@Suppress("MayBeConstant", "ObjectPropertyName")
object Samples {
    val `slash comment` = "// comment"
    val `hash comment` = "# comment"
    val `multi comment` = "/* comment */"
    val `doc comment` = """
        /**
         * doc comment
         */
    """.trimIndent()
    val `open comment` = "/* comment"

    val `nested slash-slash comment` = "// comment // comment"
    val `nested slash-hash comment` = "// comment # comment"
    val `nested multi-multi comment` = "/* comm /* comment */ ent */"
    val `nested slash-multi comment` = "// comm /* comment */ ent"
    val `nested multi-slash comment` = "/* comm // ent */"

    val `quotes in comment` = "// \"cool\" comment"
    val `comment in quotes` = "\"nice // quote\""
    val `invalid comment quote` = "/* comm \"ent */ wtf\""
    val `invalid quote comment` = "\"quote /* comm\" ent*/"

    val `single quote in doubles` = "\" quote '\""
    val `double quote in singles` = "' quote \"'"
    val `double quote in doc` = "\"\"\" still \"quote\" \"\"\""
    val `escaped quote` = "\" quote \\\" \""
}