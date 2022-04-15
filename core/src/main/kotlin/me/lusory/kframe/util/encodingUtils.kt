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
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.*

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

// https://stackoverflow.com/a/58118078
private val LOOKUP_TABLE_LOWER = charArrayOf(
    0x30.toChar(),
    0x31.toChar(),
    0x32.toChar(),
    0x33.toChar(),
    0x34.toChar(),
    0x35.toChar(),
    0x36.toChar(),
    0x37.toChar(),
    0x38.toChar(),
    0x39.toChar(),
    0x61.toChar(),
    0x62.toChar(),
    0x63.toChar(),
    0x64.toChar(),
    0x65.toChar(),
    0x66.toChar()
)

private val LOOKUP_TABLE_UPPER = charArrayOf(
    0x30.toChar(),
    0x31.toChar(),
    0x32.toChar(),
    0x33.toChar(),
    0x34.toChar(),
    0x35.toChar(),
    0x36.toChar(),
    0x37.toChar(),
    0x38.toChar(),
    0x39.toChar(),
    0x41.toChar(),
    0x42.toChar(),
    0x43.toChar(),
    0x44.toChar(),
    0x45.toChar(),
    0x46.toChar()
)

/**
 * Converts this byte array to a hex string.
 *
 * @param upperCase should the hex string be uppercase?
 * @param byteOrder the endianness of this byte array
 * @return the hex string
 * @since 0.0.1
 */
fun ByteArray.toHexBinary(upperCase: Boolean, byteOrder: ByteOrder): String {
    // our output size will be exactly 2x byte-array length
    val buffer = CharArray(size * 2)

    // choose lower or uppercase lookup table
    val lookup = if (upperCase) LOOKUP_TABLE_UPPER else LOOKUP_TABLE_LOWER
    var index: Int
    for (i in indices) {
        // for little endian we count from last to first
        index = if (byteOrder == ByteOrder.BIG_ENDIAN) i else size - i - 1

        // extract the upper 4 bit and look up char (0-A)
        buffer[i shl 1] = lookup[this[index].toInt() shr 4 and 0xF]
        // extract the lower 4 bit and look up char (0-A)
        buffer[(i shl 1) + 1] = lookup[this[index].toInt() and 0xF]
    }
    return String(buffer)
}

/**
 * Converts this byte array to a hex string.
 *
 * @return the hex string
 * @since 0.0.1
 */
fun ByteArray.toHexBinary(): String = toHexBinary(false, ByteOrder.BIG_ENDIAN)

/**
 * Computes a MD5 hash of the contents of this file.
 *
 * @since 0.0.1
 */
val File.md5: String
    get() = computeHash("MD5").toHexBinary()