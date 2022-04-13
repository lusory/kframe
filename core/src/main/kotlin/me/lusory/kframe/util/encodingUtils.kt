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

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.*
import javax.xml.bind.DatatypeConverter

/**
 * Encodes this string to Base64.
 *
 * @return the encoded string
 * @since 0.0.1
 */
fun String.toBase64(): String = Base64.getEncoder().encodeToString(encodeToByteArray())

/**
 * Decodes this string from Base64.
 *
 * @return the decoded string
 * @since 0.0.1
 */
fun String.fromBase64(): String = String(Base64.getDecoder().decode(this))

/**
 * Computes a hash of this file.
 *
 * @param type the hash type, directly passed to [MessageDigest] for lookups
 * @param bufferSize the digesting buffer size
 * @return the hashed bytes, use [toHexBinary] to convert it to a string
 * @since 0.0.1
 */
fun File.computeHash(type: String, bufferSize: Int = 1024): ByteArray {
    val digest: MessageDigest = MessageDigest.getInstance(type)
    val buffer = ByteArray(bufferSize)
    var numRead: Int

    FileInputStream(this).use { inputStream ->
        do {
            numRead = inputStream.read(buffer)
            if (numRead > 0) {
                digest.update(buffer, 0, numRead)
            }
        } while (numRead != -1)
    }

    return digest.digest()
}

/**
 * Converts this byte array to a hex string using [DatatypeConverter].
 *
 * @return the hex string
 * @since 0.0.1
 */
fun ByteArray.toHexBinary(): String = DatatypeConverter.printHexBinary(this)

/**
 * Computes a MD5 hash of the contents of this file.
 *
 * @since 0.0.1
 */
val File.md5: String
    get() = computeHash("MD5").toHexBinary()