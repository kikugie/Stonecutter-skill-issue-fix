package dev.kikugie.stonecutter

/**
 * Annotated members are stable parts of the API available in configuration scripts.
 * Some public members may not be annotated with this in case the functionality
 * only needs to be public for addons, but not the end user.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class StonecutterAPI

/**
 * Annotated members are still treated as public API, but may cause issues when misused.
 */
@RequiresOptIn("This functionality is prone to cause issues when used incorrectly. Proceed with caution.")
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
public annotation class StonecutterDelicate