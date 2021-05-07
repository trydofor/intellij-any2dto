package com.moilioncircle.intellij.any2dto.helper

import pro.fessional.meepo.eval.fmt.Case

/**
 * @author trydofor
 * @since 2020-12-31
 */
object NamingFuns {

    fun camelCase(name: String) = Case.funCamelCase.eval(emptyMap(), name) as String
    fun pascalCase(name: String) = Case.funPascalCase.eval(emptyMap(), name) as String
    fun bigSnake(name: String) = Case.funBigSnake.eval(emptyMap(), name) as String
    fun snakeCase(name: String) = Case.funSnakeCase.eval(emptyMap(), name) as String
}
