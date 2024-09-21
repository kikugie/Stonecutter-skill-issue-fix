package dev.kikugie.stonecutter.process

import java.util.concurrent.atomic.AtomicInteger

internal class ProcessStatistics {
    val total = AtomicInteger()
    val processed = AtomicInteger()
    val parsed = AtomicInteger()
}