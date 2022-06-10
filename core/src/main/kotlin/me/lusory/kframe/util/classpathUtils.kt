/*
 * This file is part of kframe, licensed under the Apache License, Version 2.0 (the "License").
 *
 * Copyright (c) 2022-present lusory contributors
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.lusory.kframe.util

import java.io.InputStream

/**
 * A placeholder object for retrieving class loaders via [classLoaders].
 */
object RuntimeProvider {
    /**
     * The class loader of the [RuntimeProvider] object and the current thread context class loader, if available.
     */
    val classLoaders: Set<ClassLoader>
        get() {
            val ctxLoader: ClassLoader? = Thread.currentThread().contextClassLoader
            if (ctxLoader != null) {
                return setOf(ctxLoader, javaClass.classLoader)
            }
            return setOf(javaClass.classLoader)
        }

    /**
     * Retrieves a classpath resource from the [RuntimeProvider] or the current thread context class loader by its name.
     *
     * @param name the resource name, does not need to be prefixed with `/`
     * @return the resource stream, null if not found
     * @since 0.0.1
     */
    fun getClasspathResource(name: String): InputStream? {
        classLoaders.forEach { cl -> cl.getClasspathResource(name)?.let { return it } }
        return null
    }
}

/**
 * Retrieves a classpath resource in this [ClassLoader] by its name.
 *
 * @param name the resource name, does not need to be prefixed with `/`
 * @return the resource stream, null if not found
 * @since 0.0.1
 */
fun ClassLoader.getClasspathResource(name: String): InputStream? = getResourceAsStream(if (!name.startsWith('/')) "/$name" else name)