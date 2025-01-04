@file:Suppress("ClassName", "FunctionName", "UNUSED_PARAMETER")

package stonecutter_samples

import dev.kikugie.stonecutter.settings.StonecutterSettings

private val rootProject: String = TODO("This is a sample, it must not be instantiated.") // Project in real case
private val subProject: String = TODO("This is a sample, it must not be instantiated.") // Project in real case
private val stonecutter: StonecutterSettings = TODO("This is a sample, it must not be instantiated.")
private fun stonecutter(action: StonecutterSettings.() -> Unit) {}
private fun property(name: String): Any = TODO("This is a sample, it must not be instantiated.")

object create {
    fun shared_single_project() {
        stonecutter {
            shared {
                versions("1.20.1", "1.21.1")
            }
            create(rootProject)
        }
    }

    fun shared_single_path() {
        stonecutter {
            shared {
                versions("1.20.1", "1.21.1")
            }
            create("subproject")
        }
    }

    fun shared_vararg_project() {
        stonecutter {
            shared {
                versions("1.20.1", "1.21.1")
            }
            create(rootProject, subProject)
        }
    }

    fun shared_iterable_project() {
        val projects = listOf(rootProject, subProject)
        stonecutter {
            shared {
                versions("1.20.1", "1.21.1")
            }
            create(projects)
        }
    }

    fun shared_vararg_path() {
        stonecutter {
            shared {
                versions("1.20.1", "1.21.1")
            }
            create("subproject1", "subproject2")
        }
    }

    fun shared_iterable_path() {
        val projects = listOf("subproject1", "subproject2")
        stonecutter {
            shared {
                versions("1.20.1", "1.21.1")
            }
            create(projects)
        }
    }
}

object settings {
    fun single() {
        stonecutter {
            create(rootProject) {
                vers("1.21-snapshot", "1.21-rc.1")
            }
        }
    }

    fun basic_vararg() {
        stonecutter {
            create(rootProject) {
                versions("1.20.1", "1.21.1")
            }
        }
    }

    fun basic_iterable() {
        // setup.versions=1.20.1 1.21.1
        val propertyVersions: List<String> = property("setup.versions")
            .toString().split(' ')
        stonecutter {
            create(rootProject) {
                versions(propertyVersions)
            }
        }
    }

    fun pairs_vararg() {
        stonecutter {
            create(rootProject) {
                versions(
                    "1.20.1" to "1.21.1",
                    "1.21-snapshot" to "1.21-rc.1"
                )
            }
        }
    }

    fun pairs_iterable() {
        // setup.versions=1.20.1=1.20.1 1.21-snapshot=1.21-rc.1
        val propertyVersions: List<Pair<String, String>> = property("setup.versions")
            .toString().split(' ')
            .map {
                val (project, version) = it.split('=')
                project to version
            }
        stonecutter {
            create(rootProject) {
                versions(propertyVersions)
            }
        }
    }
}