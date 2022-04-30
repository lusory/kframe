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

package me.lusory.kframe.processor

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSModifierListOwner
import com.google.devtools.ksp.symbol.Modifier
import java.util.*

/**
 * Clears the collection if all items are logically empty (empty string).
 *
 * @since 0.0.1
 */
fun MutableCollection<String>.clearIfLogicallyEmpty() = clearIf { it.isEmpty() }

/**
 * Clears the collection if all items match the supplied predicate.
 *
 * @param predicate the predicate
 * @since 0.0.1
 */
inline fun <T> MutableCollection<T>.clearIf(predicate: (T) -> Boolean) {
    if (all(predicate)) {
        clear()
    }
}

/**
 * Decodes this string from Base64.
 *
 * @return the decoded string
 * @since 0.0.1
 */
fun String.fromBase64(): String = String(Base64.getDecoder().decode(this))

/**
 * Returns null if this string is empty.
 *
 * @return null if empty, otherwise no change
 * @since 0.0.1
 */
fun String.nullIfEmpty(): String? = ifEmpty { null }

/**
 * Checks if this [KSModifierListOwner] is accessible to an outside class (not private, protected or internal).
 *
 * @since 0.0.1
 */
val KSModifierListOwner.isAccessible: Boolean
    get() = modifiers.none { it == Modifier.PRIVATE || it == Modifier.PROTECTED || it == Modifier.INTERNAL }

/**
 * Checks if the annotation with the supplied fully qualified name is present on this [KSAnnotated].
 *
 * @param qualifiedName the fully qualified name of the annotation (e.g. me.lusory.kframe.inject.Component)
 * @return is the annotation present?
 * @since 0.0.1
 */
fun KSAnnotated.isAnnotationPresent(qualifiedName: String): Boolean {
    val simpleName: String = qualifiedName.substringAfterLast('.')

    return annotations.any {
        it.shortName.getShortName() == simpleName
                && it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
    }
}

/**
 * Lists all annotations with the supplied fully qualified name present on this [KSAnnotated].
 *
 * @param qualifiedName the fully qualified name of the annotation (e.g. me.lusory.kframe.inject.Component)
 * @return the sequence of annotations
 * @since 0.0.1
 */
fun KSAnnotated.getAnnotationsByType(qualifiedName: String): Sequence<KSAnnotation> {
    val simpleName: String = qualifiedName.substringAfterLast('.')

    return annotations.filter {
        it.shortName.getShortName() == simpleName
                && it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
    }
}