package com.moilioncircle.intellij.any2dto.helper

/**
 * @author trydofor
 * @since 2020-12-31
 */
object NamingFuns {

    private val funReg = """\{\s*(tab|ref|col)(?:\s*\|\s*(PascalCase|camelCase|BIG_SNAKE|snake_case))?\s*\}""".toRegex()

    fun mergeFun(ptn: List<Pair<String, String>>, arg: Map<String, String>): String {
        val sb = StringBuilder()
        for ((k, f) in ptn) {
            if (k.isEmpty()) {
                sb.append(f)
            } else {
                val v = arg[k]
                if (v == null) {
                    sb.append("{$k|$f}")
                } else {
                    val s = when (f) {
                        "PascalCase" -> pascalCase(v)
                        "camelCase" -> camelCase(v)
                        "BIG_SNAKE" -> bigSnake(v)
                        "snake_case" -> snakeCase(v)
                        "" -> v
                        else -> "{$k|$f}"
                    }
                    sb.append(s)
                }
            }
        }
        return sb.toString()
    }

    fun parseFun(ptn: String): List<Pair<String, String>> {
        val rst = ArrayList<Pair<String, String>>()
        var off = 0
        for (mr in funReg.findAll(ptn)) {
            val s = mr.range.first
            if (s > off) {
                rst.add("" to ptn.substring(off, s))
            }
            val (k, f) = mr.destructured
            rst.add(k to f)
            off = mr.range.last + 1
        }

        if (rst.isEmpty()) {
            rst.add("" to ptn)
        } else {
            if (off < ptn.length - 1) {
                rst.add("" to ptn.substring(off))
            }
        }
        return rst
    }

    fun camelCase(name: String): String {
        val buff = StringBuilder(name.length + 10)
        var flag = 0
        var last = '\u0000'
        for (c in name) {
            flag = if (c.isJavaIdentifierPart()) {
                if (c == '_' || c == '-') {
                    1
                } else {
                    when (flag) {
                        0 -> buff.append(c.toLowerCase())
                        1 -> buff.append(c.toUpperCase())
                        else -> {
                            if (last.isUpperCase()) {
                                buff.append(c.toLowerCase())
                            } else {
                                buff.append(c)
                            }
                        }
                    }
                    -1
                }
            } else {
                if (flag == 0) 0 else 1
            }
            last = c
        }
        return buff.toString()
    }

    fun pascalCase(name: String): String {
        val camelCase = camelCase(name)
        return if (camelCase.length > 1) {
            camelCase[0].toUpperCase() + camelCase.substring(1)
        } else {
            camelCase.toUpperCase()
        }
    }

    fun bigSnake(name: String): String {
        val buff = StringBuilder(name.length + 10)
        var flag = 0
        var last = '\u0000'
        for (c in name) {
            flag = if (c.isJavaIdentifierPart()) {
                if (c == '_' || c == '-') {
                    1
                } else {
                    when (flag) {
                        0 -> buff.append(c.toUpperCase())
                        1 -> buff.append('_').append(c.toUpperCase())
                        else -> {
                            if (c.isUpperCase() && last.isLowerCase()) {
                                buff.append('_').append(c)
                            } else {
                                buff.append(c.toUpperCase())
                            }
                        }
                    }
                    -1
                }
            } else {
                if (flag == 0) 0 else 1
            }
            last = c
        }
        return buff.toString()
    }

    fun snakeCase(name: String) = bigSnake(name).toLowerCase()
}