package com.moilioncircle.intellij.any2dto.sqldsl

import com.moilioncircle.intellij.any2dto.helper.NamingFuns

/**
 * @author trydofor
 * @since 2020-12-31
 */
class DslMerger(
    val col: String, // usage:{col|fun}; fun:PascalCase|camelCase|BIG_SNAKE|snake_case;
    val tab: String, // usage:{var|fun}; var:tab|ref; fun:PascalCase|camelCase|BIG_SNAKE|snake_case;
    val dsl: String, // usage:{tab|fun}; tab=then 1st table; fun:PascalCase|camelCase|BIG_SNAKE|snake_case;
    val ctx: String, // ctx
) {
    private val tabPtn = NamingFuns.parseFun(tab)
    private val dslPtn = NamingFuns.parseFun(dsl)

    fun merge(sql: String): String {
        val visitor = SqlVisitors(col)
        visitor.visitSql(sql)

        // todos
        val sb = StringBuilder()
        val todos = visitor.unImpls
        val notes = visitor.warning
        if (todos.size + notes.size > 0) {
            sb.append("/* == Notice & Warning ==\n")
            for (note in notes) {
                sb.append(note).append("\n")
            }
            for (todo in todos) {
                val (part, item) = todo
                sb.append("NotSupport=").append(part)
                    .append(", value=").append(item.toString())
                    .append("\n")
            }
            sb.append("*/\n")
        }

        // alias and refer
        val arg = HashMap<String, String>()
        val (jooqDsl, refTabs) = visitor.jooqDsl()

        // dsl
        if(refTabs.isNotEmpty()) {
            arg["tab"] = refTabs.iterator().next().second
        }
        val dslSts = NamingFuns.mergeFun(dslPtn, arg)
        sb.append(dslSts).append(";\n")

        // tables
        for ((ref, tab, uas) in refTabs) {
            arg["tab"] = tab
            arg["ref"] = ref
            val table = NamingFuns.mergeFun(tabPtn, arg)
            sb.append(table)
            if (uas) {
                sb.append(""".as("$ref")""")
            }
            sb.append(";\n")
        }
        sb.append(ctx).append(".")
        sb.append(jooqDsl.trim())
        sb.append("\n.getSQL(); // .fetch() or .execute()")

        return sb.toString()
    }
}