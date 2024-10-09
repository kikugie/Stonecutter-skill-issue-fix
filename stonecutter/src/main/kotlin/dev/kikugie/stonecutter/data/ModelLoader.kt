package dev.kikugie.stonecutter.data

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.KSerializer
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Interface responsible for loading and saving models to and from the disk in YAML format.
 *
 * @param T The type of the model being loaded or saved.
 * @see TreeModel
 * @see BranchModel
 * @see NodeModel
 */
interface ModelLoader<T> {
    /**
     * Specifies the name of the YAML file used for loading and saving the model.
     */
    val filename: String

    /**
     * Saves the given model to the specified directory in YAML format.
     *
     * @param directory The path to the directory where the model will be saved.
     * @param model The model object to be saved.
     * @param serializer The serializer used to convert the model to a string.
     * @return A Result wrapping a Unit on success, or an exception if the operation fails.
     */
    fun save(directory: Path, model: T, serializer: KSerializer<T>): Result<Unit> = directory.runCatching {
        val yaml = Yaml.default.encodeToString(serializer, model)
        createDirectories()
        resolve(filename).writeText(
            yaml,
            Charsets.UTF_8,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    /**
     * Loads a model from a specified directory using a given serializer.
     *
     * @param directory The path to the directory from which the model will be loaded.
     * @param serializer The serializer used to deserialize the model from a string.
     * @return A Result wrapping the loaded model on success or an exception if the operation fails.
     */
    fun load(directory: Path, serializer: KSerializer<T>): Result<T> = directory.runCatching {
        val text = resolve(filename).readText(Charsets.UTF_8)
        Yaml.default.decodeFromString(serializer, text)
    }
}