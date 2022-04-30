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

import java.util.*

/**
 * Gets a system property.
 *
 * @param name the property name
 * @return the property value, null if not found
 * @since 0.0.1
 */
fun property(name: String): String? = System.getProperty(name)

/**
 * Gets system properties.
 *
 * @return the properties
 * @since 0.0.1
 */
fun properties(): Properties = System.getProperties()