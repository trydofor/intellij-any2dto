package com.moilioncircle.intellij.any2dto.helper

import com.moilioncircle.intellij.any2dto.helper.ConfigHelper.TypeMapping
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author trydofor
 * @since 2020-12-19
 */
class ConfigHelperTest {

    @Test
    fun parseMapping() {
        assertEquals(TypeMapping("java.math.BigDecimal", "DECIMAL", -1, -1, -1, -1),
            ConfigHelper.parseMapping(" DECIMAL = java.math.BigDecimal "))

        assertEquals(TypeMapping("java.math.BigDecimal", "DECIMAL", -1, -1, -1, -1),
            ConfigHelper.parseMapping("DECIMAL() = java.math.BigDecimal"))

        assertEquals(TypeMapping("java.math.BigDecimal", "DECIMAL", 10, 10, -1, -1),
            ConfigHelper.parseMapping("DECIMAL(10) = java.math.BigDecimal"))

        assertEquals(TypeMapping("java.math.BigDecimal", "DECIMAL", 10, 10, -1, -1),
            ConfigHelper.parseMapping("DECIMAL(10,) = java.math.BigDecimal"))

        assertEquals(TypeMapping("java.math.BigDecimal", "DECIMAL", 10, 10, -1, -1),
            ConfigHelper.parseMapping("DECIMAL(10,*) = java.math.BigDecimal"))

        assertEquals(TypeMapping("java.math.BigDecimal", "DECIMAL", 10, 10, 1, 3),
            ConfigHelper.parseMapping("DECIMAL(10,1-3) = java.math.BigDecimal"))
    }

    @Test
    fun parseRange() {
        assertEquals(Pair(-1, -1), ConfigHelper.parseRange("*"))
        assertEquals(Pair(1, 1), ConfigHelper.parseRange("1"))
        assertEquals(Pair(1, -1), ConfigHelper.parseRange("1-"))
        assertEquals(Pair(-1, 1), ConfigHelper.parseRange("-1"))
        assertEquals(Pair(-1, -1), ConfigHelper.parseRange("-"))
        assertEquals(Pair(-1, -1), ConfigHelper.parseRange("*-"))
        assertEquals(Pair(-1, -1), ConfigHelper.parseRange("*-*"))
        assertEquals(Pair(-1, -1), ConfigHelper.parseRange("-*"))
    }
}
