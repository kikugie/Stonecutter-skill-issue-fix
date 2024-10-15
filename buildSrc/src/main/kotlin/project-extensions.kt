import net.lingala.zip4j.ZipFile
import org.gradle.api.Project
import org.intellij.lang.annotations.Language
import java.io.File

fun File.unzip(to: File) = ZipFile(this).use { zip ->
    to.mkdirs()
    zip.extractAll(to.absolutePath)
}

fun Project.replace(path: String, @Language("RegExp") pattern: String, replacement: String) {
    val location = project.file(path)
    require(location.exists() && location.isFile && location.canRead()) { "Path $path is invalid" }
    val text = location.readText()
    val new = text.replace(Regex(pattern, RegexOption.MULTILINE), replacement)
    if (new != text) location.writeText(new)
}

fun Project.rename(dir: String, @Language("RegExp") pattern: String, newName: String) {
    val location = project.file(dir)
    location.listFiles()?.find { it.name.matches(Regex(pattern)) }?.renameTo(location.resolve(newName))
}