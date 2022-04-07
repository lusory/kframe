package me.lusory.kframe.test.inject

import me.lusory.kframe.inject.applicationContext
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ApplicationContextTest {
    @Test
    fun buildContext() {
        val expected: Set<Any> = setOf(object {}, object {})

        assertContentEquals(
            expected,
            actual = applicationContext { for (obj in expected) { newComponent { obj } } }.components.asIterable()
        )

        assertContentEquals(
            expected,
            actual = applicationContext { for (obj in expected) { newComponentProvider { obj }() } }.components.asIterable()
        )
    }
}