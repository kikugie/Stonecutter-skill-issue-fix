package dev.kikugie.stonecutter

@Suppress("unused", "MemberVisibilityCanBePrivate")
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class StonecutterAPI

@RequiresOptIn("This functionality is prone to cause issues when used incorrectly. Proceed with caution.")
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class StonecutterDelicate