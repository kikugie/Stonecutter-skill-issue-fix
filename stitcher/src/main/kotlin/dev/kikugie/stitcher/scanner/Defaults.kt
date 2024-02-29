package dev.kikugie.stitcher.scanner

data object StandardMultiLine : CommentRecognizer {
    override val start = "/*"
    override val end = "*/"
}
data object StandardSingleLine : CommentRecognizer {
    override val start = "//"
    override val end = "\n"
}
data object HashSingleLine : CommentRecognizer {
    override val start = "#"
    override val end = "\n"
}