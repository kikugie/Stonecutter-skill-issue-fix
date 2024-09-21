package dev.kikugie.stonecutter.process

// Instructs the error printer to not add the stack trace
internal class ProcessException(message: String) : Exception(message)