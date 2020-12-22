package com.moilioncircle.intellij.any2dto.helper

/**
 * @author trydofor
 * @since 2020-12-16
 */
object ConfigHelper {

    val defaultMapping = loadDefaultConf("java-type-mapping.txt")
    val defaultTemplateInner = loadDefaultConf("java-templet-inner.java")
    val defaultTemplateOuter = loadDefaultConf("java-templet-outer.java")

    /**
     * TYPE_NAME(PRECISION,SCALE) = JAVA_CLASS
     */
    data class TypeMapping(
        val javaType: String,
        val sqlType: String,
        val sqlPrecisionBgn: Int,
        val sqlPrecisionEnd: Int,
        val sqlScaleBgn: Int,
        val sqlScaleEnd: Int,
    )

    private val emptyMapping = TypeMapping("", "", 0, 0, 0, 0)
    private val regexMapping = """([^(]+)(?:\(([^=]*)\))?=(\S+)""".toRegex()

    fun parseMapping(rule: String): TypeMapping {
        val txt = rule.substringBefore("#")
            .replace(" ", "")
            .replace("\t", "")

        val mp = regexMapping.matchEntire(txt)?.destructured ?: return emptyMapping
        val (sqlType, sqlSize, javaType) = mp

        return if (sqlSize.isEmpty()) {
            TypeMapping(javaType, sqlType, -1, -1, -1, -1)
        } else {
            val rng = sqlSize.split(",")
            val (p1, p2) = parseRange(rng[0])
            if (rng.size > 1) {
                val (s1, s2) = parseRange(rng[1])
                TypeMapping(javaType, sqlType, p1, p2, s1, s2)
            } else {
                TypeMapping(javaType, sqlType, p1, p2, -1, -1)
            }
        }
    }

    fun parseRange(str: String): Pair<Int, Int> {
        if (str.isEmpty() || str == "*") return Pair(-1, -1)
        val pts = str.split("-")
        val p1 = parseInteger(pts[0])
        val p2 = if (pts.size > 1) {
            parseInteger(pts[1])
        } else {
            p1
        }

        return Pair(p1, p2)
    }

    private fun parseInteger(num: String): Int = try {
        if (num.isEmpty() || num == "*") {
            -1
        } else {
            num.toInt()
        }
    } catch (e: Exception) {
        -1
    }

    private fun loadDefaultConf(name: String) = ConfigHelper.javaClass
        .getResourceAsStream("/config/$name")
        .reader()
        .readText()
}