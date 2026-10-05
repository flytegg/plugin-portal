package gg.flyte.pluginportal.common.runtime

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** Small YAML document abstraction shared by both platform adapters. */
class YamlConfiguration private constructor(private var values: MutableMap<String, Any?>, private var separator: Char) {
    constructor() : this(linkedMapOf(), '.')
    companion object {
        fun loadConfiguration(file: File) = YamlConfiguration().apply {
            if (file.exists()) loadFromString(file.readText())
        }
    }
    fun options() = this
    fun pathSeparator(value: Char) = apply { separator = value }

    @Synchronized fun loadFromString(content: String) {
        val loaded = Yaml(SafeConstructor(LoaderOptions())).load<Any?>(content)
        require(loaded == null || loaded is Map<*, *>) { "YAML root must be a mapping" }
        @Suppress("UNCHECKED_CAST")
        values = (loaded as? Map<String, Any?>)?.toMutableMap() ?: linkedMapOf()
    }
    @Synchronized fun get(path: String): Any? {
        var current: Any? = values
        path.split(separator).forEach { part -> current = (current as? Map<*, *>)?.get(part) }
        return current
    }
    fun contains(path: String) = get(path) != null
    fun getString(path: String, default: String? = null): String? = get(path)?.toString() ?: default
    fun getBoolean(path: String, default: Boolean = false): Boolean = get(path) as? Boolean ?: default
    fun getStringList(path: String): List<String> = (get(path) as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    @Synchronized fun getKeys(deep: Boolean): Set<String> {
        require(!deep) { "Only direct YAML keys are supported" }
        return values.keys.toSet()
    }
    fun getConfigurationSection(path: String): YamlConfiguration? {
        @Suppress("UNCHECKED_CAST")
        val section = get(path) as? Map<String, Any?> ?: return null
        return YamlConfiguration(section.toMutableMap(), separator)
    }
    @Synchronized fun set(path: String, value: Any?) {
        val parts = path.split(separator)
        var current = values
        for (part in parts.dropLast(1)) {
            @Suppress("UNCHECKED_CAST")
            val child = (current[part] as? Map<String, Any?>)?.toMutableMap() ?: linkedMapOf()
            current[part] = child
            current = child
        }
        if (value == null) current.remove(parts.last()) else current[parts.last()] = value
    }
    @Synchronized fun save(file: File) {
        file.absoluteFile.parentFile.mkdirs()
        val temporary = Files.createTempFile(file.absoluteFile.parentFile.toPath(), ".${file.name}", ".tmp")
        try {
            val options = DumperOptions().apply { defaultFlowStyle = DumperOptions.FlowStyle.BLOCK }
            Files.writeString(temporary, Yaml(options).dump(values))
            Files.move(temporary, file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally { Files.deleteIfExists(temporary) }
    }
}
