package dev.kikugie.stonecutter.controller.storage

import dev.kikugie.stonecutter.Identifier
import dev.kikugie.stonecutter.controller.ControllerParameters
import dev.kikugie.stonecutter.controller.StonecutterController
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.ApiStatus
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties

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
     * Creates a Kotlin property for the given [name].
     * ```kotlin
     * var receiverName: String by parameters.named("receiver")
     * ```
     */
    inline fun <reified T : Any> named(name: String): ReadWriteProperty<Any, T> =
        provider(name, T::class)

    /**
     * Creates a Kotlin property for the given [name], with specified [verifier] for the setter.
     * ```kotlin
     * var receiverName: String by parameters.named("receiver") {
     *     require(it != "minceraft") { "No rafts allowed!" }
     * }
     * ```
     */
    inline fun <reified T : Any> named(name: String, crossinline verifier: (T) -> Unit): ReadWriteProperty<Any, T> =
        object : ReadWriteProperty<Any, T> {
            private val provider: ReadWriteProperty<Any, T> = provider(name, T::class)
            override fun getValue(thisRef: Any, property: KProperty<*>): T = provider.getValue(thisRef, property)
            override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = verifier.invoke(value).let {
                provider.setValue(thisRef, property, value)
            }
        }

    @ApiStatus.Internal
    fun <T : Any> provider(name: String, type: KClass<T>): ReadWriteProperty<Any, T> {
        val property = requireNotNull(this::class.declaredMemberProperties.find { it.name == name }) {
            "Property $name not found"
        }
        require(property is KMutableProperty1) { "Property $name is not mutable" }
        require(property.returnType.classifier == type) {
            "Property $name is not of the required type ${type.simpleName}"
        }
        @Suppress("UNCHECKED_CAST")
        return Provider(property as KMutableProperty1<GlobalParameters, T>)
    }

    private inner class Provider<T>(private val property: KMutableProperty1<GlobalParameters, T>) :
        ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T = this.property(this@GlobalParameters)
        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
            this.property.set(this@GlobalParameters, value)
    }
}