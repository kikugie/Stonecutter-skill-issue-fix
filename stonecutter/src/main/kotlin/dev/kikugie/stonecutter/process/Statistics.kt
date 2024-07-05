package dev.kikugie.stonecutter.process

class Statistics {
    var total: Int = 0
        set(value) = synchronized(this) { field = value }
    var skipped: Int = 0
        set(value) = synchronized(this) { field = value }
    var parsed: Int = 0
        set(value) = synchronized(this) { field = value }
    var duration: Long = 0
        set(value) = synchronized(this) { field = value }
}