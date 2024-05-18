package dev.kikugie.stitcher.process.access

typealias Expression = (String) -> Boolean?
typealias Expressions = Iterable<Expression>
typealias Constants = Map<String, Boolean>
typealias Swaps = Map<String, String>