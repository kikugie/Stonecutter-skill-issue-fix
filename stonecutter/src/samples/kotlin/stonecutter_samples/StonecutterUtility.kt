@file:Suppress("ClassName", "unused")

package stonecutter_samples

import dev.kikugie.stonecutter.StonecutterUtility

private val stonecutter: StonecutterUtility = TODO("This is a sample, it must not be instantiated.")

object eval {
    fun strict() {
        stonecutter.eval("1.1", ">=1.0 <2.0") // == true
        stonecutter.eval("v1", ">=1.0 <2.0") // -> VersionParsingException
    }

    fun lenient() {
        stonecutter.eval("v0", ">=v1 <v3") // == false
        stonecutter.eval("v2", ">=v1 <v3") // == true
    }
}