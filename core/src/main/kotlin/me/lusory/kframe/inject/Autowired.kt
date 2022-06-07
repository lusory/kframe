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

package me.lusory.kframe.inject

/**
 * Marks a constructor or a property as a dependency injection candidate.
 *
 * This is not necessary for primary constructors or when the class has just one constructor.
 *
 * When a property is annotated and its type subclasses [Collection], all components which subclass the element type of the collection are injected on runtime into the property.
 * **This annotation does not do anything for non-Collection properties.**
 *
 * @since 0.0.1
 */
@Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Autowired
