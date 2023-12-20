package dev.kikugie.stonecutter.version

import org.gradle.api.Project
import java.lang.reflect.Method
import java.net.URLClassLoader
import java.nio.file.Files
import java.util.function.Predicate
import kotlin.io.path.Path
import kotlin.io.path.exists

class FabricVersionChecker @Throws(
    ClassNotFoundException::class,
    NoSuchMethodException::class
) constructor(classLoader: ClassLoader) : VersionChecker {
    val semverParse: Method
    val predicateParse: Method

    init {
        semverParse = classLoader.loadClass("net.fabricmc.loader.api.SemanticVersion")
            .getDeclaredMethod("parse", String::class.java)
            ?: throw IllegalArgumentException("Invalid Fabric Loader jar")
        predicateParse = classLoader.loadClass("net.fabricmc.loader.api.metadata.version.VersionPredicate")
            .getDeclaredMethod("parse", String::class.java)
            ?: throw IllegalArgumentException("Invalid Fabric Loader jar")
    }

    override fun parseVersion(version: String): Version = semverParse.invoke(null, version)

    @Suppress("UNCHECKED_CAST")
    override fun parsePredicate(predicate: String): Predicate<Version> =
        predicateParse.invoke(null, predicate) as Predicate<Version>

    companion object {
        fun create(project: Project): VersionChecker {
            val path = Path(project.rootDir.path, ".gradle/stonecutter")
            Files.createDirectories(path)
            val loaderCopy = path.resolve("fabric-loader.jar")

            if (loaderCopy.exists()) try {
                return FabricVersionChecker(
                    URLClassLoader(
                        arrayOf(loaderCopy.toUri().toURL()),
                        VersionChecker::class.java.classLoader
                    )
                )
            } catch (ignored: Exception) {
            }

            project.logger.error("[Stonecutter] Could not create Fabric Loader version checker")
            return object : VersionChecker {
                override fun parseVersion(version: String) = version

                override fun parsePredicate(predicate: String): Predicate<Version> {
                    throw UnsupportedOperationException()
                }
            }
        }
    }
}