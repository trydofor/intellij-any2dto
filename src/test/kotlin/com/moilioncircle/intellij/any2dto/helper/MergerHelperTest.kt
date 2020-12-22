package com.moilioncircle.intellij.any2dto.helper

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author trydofor
 * @since 2020-12-20
 */
class MergerHelperTest {

    @Test
    fun javaFieldName() {
        assertEquals("aBeCd", MergerHelper.javaFieldName("aBeCd"))
        assertEquals("aBeCd", MergerHelper.javaFieldName("aBe_cd"))
        assertEquals("aBeCd", MergerHelper.javaFieldName("aBE_cd"))
        assertEquals("aBeCd", MergerHelper.javaFieldName("a_be_cd"))
        assertEquals("a我beCd", MergerHelper.javaFieldName("a我be_cd"))
        assertEquals("aBeCd", MergerHelper.javaFieldName("'a-be_cd'"))
        assertEquals("aBeCd", MergerHelper.javaFieldName("A_BE_CD"))
        assertEquals("a我BeCd", MergerHelper.javaFieldName("A我BE_CD"))
        assertEquals("aBeCd", MergerHelper.javaFieldName("'A-BE_CD'"))
    }
}