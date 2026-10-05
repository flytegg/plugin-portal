package gg.flyte.pluginportal.common.managers

import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Predicate

abstract class PluginCache<T>(private val cache: MutableSet<T> = CopyOnWriteArraySet()) : MutableSet<T> by cache {
    // Java's default implementation removes through the snapshot iterator, which is immutable.
    override fun removeIf(filter: Predicate<in T>): Boolean = cache.removeIf(filter)
}
