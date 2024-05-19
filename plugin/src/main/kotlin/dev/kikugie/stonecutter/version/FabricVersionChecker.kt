package dev.kikugie.stonecutter.version

import org.gradle.api.Project
import java.lang.reflect.Method
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Predicate
import kotlin.io.path.Path
import kotlin.io.path.exists

class FabricVersionChecker @Throws(
    ClassNotFoundException::class,
    NoSuchMethodException::class
) constructor(classLoader: ClassLoader) : VersionChecker {
    private val semverParse: Method = classLoader.loadClass("net.fabricmc.loader.api.SemanticVersion")
        .getDeclaredMethod("parse", String::class.java)
        ?: throw IllegalArgumentException("Invalid Fabric Loader jar")
    private val predicateParse: Method = classLoader.loadClass("net.fabricmc.loader.api.metadata.version.VersionPredicate")
        .getDeclaredMethod("parse", String::class.java)
        ?: throw IllegalArgumentException("Invalid Fabric Loader jar")

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
                return create(loaderCopy)
            } catch (_: Exception) {
            }

            project.logger.error("[Stonecutter] Could not create Fabric Loader version checker")
            return DummyVersionChecker
        }

        fun create(loader: Path) = FabricVersionChecker(
            URLClassLoader(
                arrayOf(loader.toUri().toURL()),
                VersionChecker::class.java.classLoader
            )
        )
    }
}