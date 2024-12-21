@file:Suppress("unused", "ClassName", "FunctionName", "UNUSED_PARAMETER")

package stonecutter_samples

import dev.kikugie.stonecutter.build.StonecutterBuild

private val stonecutter: StonecutterBuild = TODO("This is a sample, it must not be instantiated.")
private fun stonecutter(action: StonecutterBuild.() -> Unit) {}
private fun property(name: String): Any {
    throw UnsupportedOperationException("Not yet implemented.")
}

object swaps {
    fun setter() {
        stonecutter {
            swaps["my_swap"] = when {
                eval(current.version, ">=1.21") -> "replacement #1"
                eval(current.version, ">=1.20") -> "replacement #2"
                else -> "replacement #3"
            }
        }
    }

    fun single() {
        stonecutter {
            val replacement = if (eval(current.version, ">=1.21")) "replacement #1" else "replacement #2"
            swap("my_swap", replacement)
        }
    }

    fun provider() {
        stonecutter {
            swap("my_swap") {
                if (eval(current.version, ">=1.21")) "replacement #1"
                else "replacement #2"
            }
        }
    }

    fun vararg() {
        stonecutter {
            val options = if (eval(current.version, ">=1.21")) "option #1" else "option #2"
            val variables = if (eval(current.version, ">=1.21")) "variable #1" else "variable #2"
            swaps("my_swap" to options, "my_other_swap" to variables)
        }
    }

    fun iterable() {
        stonecutter {
            val replacements = mutableListOf<Pair<String, String>>()
            replacements.add("my_swap" to if (eval(current.version, ">=1.21")) "option #1" else "option #2")
            replacements.add("my_other_swap" to if (eval(current.version, ">=1.21")) "variable #1" else "variable #2")
            swaps(replacements)
        }
    }

    fun map() {
        stonecutter {
            val replacements = mapOf(
                "my_swap" to if (eval(current.version, ">=1.21")) "option #1" else "option #2",
                "my_other_swap" to if (eval(current.version, ">=1.21")) "variable #1" else "variable #2"
            )
            swaps(replacements)
        }
    }
}

object constants {
    fun setter() {
        stonecutter {
            consts["my_const"] = eval(current.version, ">=1.21")
        }
    }

    fun single() {
        stonecutter {
            val state = eval(current.version, ">=1.21")
            const("my_const", state)
        }
    }

    fun provider() {
        stonecutter {
            const("my_const") {
                eval(current.version, ">=1.21")
            }
        }
    }

    fun vararg() {
        stonecutter {
            val is121 = eval(current.version, ">=1.21")
            val is120 = eval(current.version, ">=1.20")
            consts("my_const" to is121, "my_other_const" to is120)
        }
    }

    fun iterable() {
        stonecutter {
            val constants = mutableListOf<Pair<String, Boolean>>()
            constants.add("my_const" to eval(current.version, ">=1.21"))
            constants.add("my_other_const" to eval(current.version, ">=1.20"))
            consts(constants)
        }
    }

    fun map() {
        stonecutter {
            val constants = mapOf(
                "my_const" to eval(current.version, ">=1.21"),
                "my_other_const" to eval(current.version, ">=1.20")
            )
            consts(constants)
        }
    }

    fun choices_vararg() {
        stonecutter {
            val current = "option #2"
            consts(current, "option #1", "option #2", "option #3")
        }
    }

    fun choices_iterable() {
        stonecutter {
            val options = buildList { repeat(3) { add("option #$it") } }
            val current = options[1]
            consts(current, options)
        }
    }
}

object dependencies {
    fun setter() {
        stonecutter {
            dependencies["my_dependency"] = property("dependency") as String
        }
    }

    fun single() {
        stonecutter {
            val dependency = property("dependency") as String
            dependency("my_dependency", dependency)
        }
    }

    fun provider() {
        stonecutter {
            dependency("my_dependency") {
                property("dependency") as String
            }
        }
    }

    fun vararg() {
        stonecutter {
            dependencies(
                "my_dependency" to property("dependency") as String,
                "my_other_dependency" to property("other_dependency") as String
            )
        }
    }

    fun iterable() {
        stonecutter {
            val dependencies = mutableListOf<Pair<String, String>>()
            dependencies.add("my_dependency" to property("dependency") as String)
            dependencies.add("my_other_dependency" to property("other_dependency") as String)
            dependencies(dependencies)
        }
    }

    fun map() {
        stonecutter {
            val dependencies = mapOf(
                "my_dependency" to property("dependency") as String,
                "my_other_dependency" to property("other_dependency") as String
            )
            dependencies(dependencies)
        }
    }
}

object allowExtensions {
    fun vararg() {
        stonecutter.allowExtensions("yml", "yaml")
    }

    fun iterable() {
        val extensions = listOf("yml", "yaml")
        stonecutter.allowExtensions(extensions)
    }
}

object overrideExtensions {
    fun vararg() {
        stonecutter.overrideExtensions("scala", "sc")
    }

    fun iterable() {
        val extensions = listOf("scala", "sc")
        stonecutter.overrideExtensions(extensions)
    }
}

object excludeFiles {
    fun vararg() {
        stonecutter.excludeFiles("src/main/resources/properties.json5")
    }

    fun iterable() {
        val files = listOf("src/main/resources/properties.json5")
        stonecutter.excludeFiles(files)
    }
}
