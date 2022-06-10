package me.lusory.kframe.inject.runtime

import me.lusory.kframe.util.RuntimeProvider
import java.net.URL
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * The service resource prefix.
 */
const val SERVICE_PREFIX = "META-INF/services/"

/**
 * Discovers service implementations of a super type, like [ServiceLoader].
 *
 * @param klass the super type (superinterface or a superclass of the searched implementation)
 * @param classLoaders class loaders used for searching the service definitions (automatically includes the thread context class loader and the class loader of [RuntimeProvider])
 * @param lenient should non-existing classes defined in the service definitions be ignored and should the super type be verified?
 * @since 0.0.1
 */
fun <T : Any> discoverServiceImplementations(klass: KClass<T>, vararg classLoaders: ClassLoader, lenient: Boolean = false): Set<KClass<out T>> {
    val name: String = SERVICE_PREFIX + klass.qualifiedName

    val classes: MutableSet<String> = mutableSetOf()
    for (loader: ClassLoader in RuntimeProvider.classLoaders.union(classLoaders.asIterable())) {
        val urls: Enumeration<URL> = loader.getResources(name)

        while (urls.hasMoreElements()) {
            urls.nextElement().openStream().bufferedReader().lines().forEach { line ->
                if (line.isNotEmpty() && line.first() != '#') {
                    classes.add(line.substringBefore('#').trim())
                }
            }
        }
    }

    return classes.mapNotNullTo(mutableSetOf()) { cl ->
        @Suppress("UNCHECKED_CAST")
        if (lenient) {
            try {
                Class.forName(cl).kotlin
            } catch (ignored: ClassNotFoundException) {
                null
            }
        } else {
            Class.forName(cl).kotlin.also {
                if (!it.isSubclassOf(klass)) {
                    error("Discovered a service implementation '$cl', but it doesn't subclass '${klass.qualifiedName}'")
                }
            }
        } as KClass<out T>?
    }
}