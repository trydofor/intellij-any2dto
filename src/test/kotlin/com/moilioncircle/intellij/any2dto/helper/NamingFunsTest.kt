package com.moilioncircle.intellij.any2dto.helper

import org.junit.Assert.assertEquals
import org.junit.Test
import pro.fessional.meepo.sack.Holder

/**
 * @author trydofor
 * @since 2020-12-20
 */
class NamingFunsTest {

    @Test
    fun kebabCase() {
        assertEquals("MoilionCircle", NamingFuns.pascalCase("moilion-circle"))
        assertEquals("moilionCircle", NamingFuns.camelCase("moilion-circle"))
        assertEquals("MOILION_CIRCLE", NamingFuns.bigSnake("moilion-circle"))
        assertEquals("moilion_circle", NamingFuns.snakeCase("moilion-circle"))
    }

    @Test
    fun camelCase() {
        assertEquals("aBeCd", NamingFuns.camelCase("aBeCd"))
        assertEquals("aBeCd", NamingFuns.camelCase("aBe_cd"))
        assertEquals("aBeCd", NamingFuns.camelCase("aBE_cd"))
        assertEquals("aBeCd", NamingFuns.camelCase("a_be_cd"))
        assertEquals("a我beCd", NamingFuns.camelCase("a我be_cd"))
        assertEquals("aBeCd", NamingFuns.camelCase("'a-be_cd'"))
        assertEquals("aBeCd", NamingFuns.camelCase("A_BE_CD"))
        assertEquals("a我BeCd", NamingFuns.camelCase("A我BE_CD"))
        assertEquals("aBeCd", NamingFuns.camelCase("'A-BE_CD'"))
    }

    @Test
    fun snakeCase() {
        assertEquals("a_be_cd", NamingFuns.snakeCase("aBeCd"))
        assertEquals("a_be_cd", NamingFuns.snakeCase("aBe_cd"))
        assertEquals("a_be_c_d", NamingFuns.snakeCase("aBe_cD"))
        assertEquals("a_be_cd", NamingFuns.snakeCase("aBE_cd"))
        assertEquals("a_be_cd", NamingFuns.snakeCase("a_be_cd"))
        assertEquals("a我be_cd", NamingFuns.snakeCase("a我be_cd"))
        assertEquals("a_be_cd", NamingFuns.snakeCase("'a-be_cd'"))
        assertEquals("a我be_cd", NamingFuns.snakeCase("A我BE_Cd"))
        assertEquals("a_be_cd", NamingFuns.snakeCase("'A-BE_CD'"))
    }

    @Test
    fun mergeFun() {
        val str = "tab.PascalCase={tab|PascalCase}" +
                ",ref.camelCase={ref|camelCase}" +
                ",col.BIG_SNAKE={col|BIG_SNAKE}" +
                ",col.snake_case={col|snake_case}" +
                ",col={col}" +
                "the end"
        val pse = Holder.parse(true, str, "{", "}", "\\")
        val arg = mapOf(
            "tab" to "win_system_journal",
            "col" to "create_dt",
            "ref" to "GoHere",
        )
        val rst = pse.merge(arg)
        val exp = "tab.PascalCase=WinSystemJournal" +
                ",ref.camelCase=goHere" +
                ",col.BIG_SNAKE=CREATE_DT" +
                ",col.snake_case=create_dt" +
                ",col=create_dt" +
                "the end"
        assertEquals(exp, rst)

    }
}
