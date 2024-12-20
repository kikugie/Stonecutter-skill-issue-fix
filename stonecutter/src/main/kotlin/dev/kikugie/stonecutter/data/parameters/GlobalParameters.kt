package dev.kikugie.stonecutter.data.parameters

import dev.kikugie.stonecutter.Identifier
import kotlinx.serialization.Serializable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Stores [ControllerParameters] and chiseled task names to be passed to the versioned buildscript.
 *
 * @property debug Debug flag used by the file processor, set by [ControllerParameters.debug]
 * @property process Whenever Stonecutter will scan for versioned comments, set by [ControllerParameters.processFiles]
 * @property receiver Default dependency used by the file processor, set by [ControllerParameters.defaultReceiver]
 * @property chiseled Registered chiseled tasks, set by [StonecutterController.registerChiseled]
 */
@Serializable
data class GlobalParameters(
    var debug: Boolean = false,
    var process: Boolean = true,
    var receiver: Identifier = "minecraft",
    val chiseled: MutableSet<String> = mutableSetOf()
) {
    internal fun addTask(task: String) = chiseled.add(task)
    internal fun hasChiseled(stack: Iterable<String>) = stack.any { it in chiseled }

    /**
     * Creates a Kotlin property for the given [property] name.
     * ```kotlin
     * var receiverName: String by parameters.named("receiver")
     * ```
     */
    fun <T : Any> named(property: String): ReadWriteProperty<Any, T> =
        ParameterDelegate(property)

    /**
     * Creates a Kotlin property for the given [property] name, with specified [verifier] for the setter.
     * ```kotlin
     * var receiverName: String by parameters.named("receiver") {
     *     require(it != "minceraft") { "No rafts allowed!" }
     * }
     * ```
     */
    inline fun <T : Any> named(property: String, crossinline verifier: (T) -> Unit): ReadWriteProperty<Any, T> =
        object : ReadWriteProperty<Any, T> {
            private val provider: ReadWriteProperty<Any, T> = named(property)
            override fun getValue(thisRef: Any, property: KProperty<*>): T = provider.getValue(thisRef, property)
            override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = verifier.invoke(value).let {
                provider.setValue(thisRef, property, value)
            }
        }

    @Suppress("UNCHECKED_CAST")
    private inner class ParameterDelegate<T>(private val property: String): ReadWriteProperty<Any, T> {
        init {
            require(property == "debug" || property == "process" || property == "receiver") {
                "Invalid property name: $property"
            }
        }

        override fun getValue(thisRef: Any, property: KProperty<*>): T = when (this.property) {
            "debug" -> debug as T
            "process" -> process as T
            "receiver" -> receiver as T
            else -> throw UnsupportedOperationException("Checked at init")
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = when (this.property) {
            "debug" -> debug = value as Boolean
            "process" -> process = value as Boolean
            "receiver" -> receiver = value as Identifier
            else -> throw UnsupportedOperationException("Checked at init")
        }
    }
}