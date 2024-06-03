package dev.kikugie.stitchertest.util

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import dev.kikugie.stitcher.data.token.Token
import kotlinx.serialization.encodeToString

val yaml = Yaml(configuration = YamlConfiguration(encodeDefaults = false))

fun Sequence<Token>.yaml() = toList().yaml()
fun List<Token>.yaml() = yaml.encodeToString(this)

inline fun <reified T> T.yaml() = yaml.encodeToString(this)