package com.moilioncircle.intellij.any2dto.sqldsl

import com.moilioncircle.intellij.any2dto.helper.NamingFuns
import net.sf.jsqlparser.expression.*
import net.sf.jsqlparser.expression.operators.arithmetic.*
import net.sf.jsqlparser.expression.operators.conditional.AndExpression
import net.sf.jsqlparser.expression.operators.conditional.OrExpression
import net.sf.jsqlparser.expression.operators.relational.*
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.*
import net.sf.jsqlparser.statement.alter.Alter
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence
import net.sf.jsqlparser.statement.comment.Comment
import net.sf.jsqlparser.statement.create.index.CreateIndex
import net.sf.jsqlparser.statement.create.schema.CreateSchema
import net.sf.jsqlparser.statement.create.sequence.CreateSequence
import net.sf.jsqlparser.statement.create.table.CreateTable
import net.sf.jsqlparser.statement.create.view.AlterView
import net.sf.jsqlparser.statement.create.view.CreateView
import net.sf.jsqlparser.statement.delete.Delete
import net.sf.jsqlparser.statement.drop.Drop
import net.sf.jsqlparser.statement.execute.Execute
import net.sf.jsqlparser.statement.grant.Grant
import net.sf.jsqlparser.statement.insert.Insert
import net.sf.jsqlparser.statement.merge.Merge
import net.sf.jsqlparser.statement.replace.Replace
import net.sf.jsqlparser.statement.select.*
import net.sf.jsqlparser.statement.truncate.Truncate
import net.sf.jsqlparser.statement.update.Update
import net.sf.jsqlparser.statement.upsert.Upsert
import net.sf.jsqlparser.statement.values.ValuesStatement
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author trydofor
 * @since 2020-12-30
 */
class SqlVisitors(col: String) {

    val unImpls = ArrayList<Pair<String, Any>>()
    val warning = HashSet<String>()

    private val refTabs = HashMap<String, Triple<String, Boolean, Boolean>>() // alias, table, isAlias
    private val buff = StringBuilder()
    private val counter = AtomicInteger(1)

    private val nilTab = unRef("NIL-TAB")
    private val colPtn = NamingFuns.parseFun(col)
    private val openClause = """,\s*\)""".toRegex()

    fun visitSql(sql: String) {
        unImpls.clear()
        refTabs.clear()
        warning.clear()
        buff.setLength(0)
        counter.set(1)

        val select = CCJSqlParserUtil.parse(sql)
        select.accept(vztStatement)
    }

    fun jooqDsl(): Pair<String, Set<Triple<String, String, Boolean>>> {
        val una = HashMap<String, String>()
        val set = HashSet<Triple<String, String, Boolean>>()

        for ((ref, inf) in refTabs) {
            when {
                ref == nilTab -> una[nilTab] = ""
                isUnRef(ref) -> {
                    val k = genRef(ref)
                    if (!refTabs.containsKey(inf.first)) {
                        set.add(Triple(k, inf.first, inf.second))
                    }
                    una[ref] = k
                }
                else -> set.add(Triple(ref, inf.first, inf.second))
            }
        }

        if (una[nilTab] != null) {
            val itr = refTabs.filter { it.value.third }.iterator()
            if(itr.hasNext()) {
                val it = itr.next()
                val rv = una[it.key]
                if (rv == null) {
                    una[nilTab] = it.key
                } else {
                    una[nilTab] = rv
                }
            }
        }

        // result
        var dsl = openClause.replace(buff, ")")
        for ((u, a) in una) {
            dsl = dsl.replace(u, a)
        }

        return dsl to set
    }

    private fun trim(str: String): String {
        val p1 = str.indexOf("`")
        var p2 = str.lastIndexOf("`")
        if (p1 > 0 || p2 > 0) {
            if (p2 < 0) p2 = str.length
            return str.substring(p1+1, p2)
        } else {
            return str
        }
    }

    private fun genRef(tkn: String): String {
        val tk = refTabs[tkn]!!
        val r1 = tk.first
        // from column or table
        if (!tk.third || refTabs.containsKey(r1)) return r1

        while (true) {
            val k = "t" + counter.getAndIncrement()
            if (!refTabs.containsKey(k)) return k
        }
    }

    private fun unRef(tab: String) = "/*{$tab}*/"

    private fun isUnRef(ref: String) = ref.startsWith("/*{") && ref.endsWith("}*/")

    private fun refTab(tab: Table?, from: Boolean): String {
        // 无别名多表存在问题
        if (tab == null) {
            refTabs.putIfAbsent(nilTab, Triple("", false, false))
            return nilTab
        } else {
            val tn = trim(tab.name)
            val ta = tab.alias?.name
            if (ta == null) {
                for ((r, t) in refTabs) {
                    if (t.first == tn) return r
                }
                val sa = unRef(tn)
                if(from) {
                    refTabs[sa] = Triple(tn, false, from)
                }else{
                    refTabs.putIfAbsent(sa, Triple(tn, false, from))
                }
                return sa
            } else {
                val v = trim(ta)
                if(from) {
                    refTabs[v] = Triple(tn, true, from)
                }else{
                    refTabs.putIfAbsent(v, Triple(tn, true, from))
                }
                return v
            }
        }
    }

    private fun colName(col: String): String = NamingFuns.mergeFun(colPtn, mapOf("col" to col))

    private fun unImpl(from: String, item: Any?) {
        if (item == null) return
        unImpls.add(Pair(from, item))
    }

    private fun ignore() {}

    //
    private fun vztJoin(join: Join) {
        buff.append("\n.")
        if (join.isInner) {
            buff.append("innerJoin(")
        } else if (join.isLeft) {
            buff.append("leftOuterJoin(")
        } else if (join.isRight) {
            buff.append("rightOuterJoin(")
        } else if (join.isCross) {
            buff.append("crossJoin(")
        } else {
            buff.append("join(")
        }
        join.rightItem.accept(vztFromItem)
        buff.append(")\n.on(")
        join.onExpression.accept(vztExpression)
        buff.append(")")
    }

    private val vztStatement = object : StatementVisitor {
        override fun visit(comment: Comment) {
            buff.append("\n// ${comment.comment} \n")
        }

        override fun visit(commit: Commit?) = unImpl("StatementVisitor", commit)
        override fun visit(delete: Delete?) = unImpl("StatementVisitor", delete)
        override fun visit(update: Update?) = unImpl("StatementVisitor", update)
        override fun visit(insert: Insert?) = unImpl("StatementVisitor", insert)
        override fun visit(replace: Replace?) = unImpl("StatementVisitor", replace)
        override fun visit(drop: Drop?) = unImpl("StatementVisitor", drop)
        override fun visit(truncate: Truncate?) = unImpl("StatementVisitor", truncate)
        override fun visit(createIndex: CreateIndex?) = unImpl("StatementVisitor", createIndex)
        override fun visit(createSchema: CreateSchema?) = unImpl("StatementVisitor", createSchema)
        override fun visit(createTable: CreateTable?) = unImpl("StatementVisitor", createTable)
        override fun visit(createView: CreateView?) = unImpl("StatementVisitor", createView)
        override fun visit(alterView: AlterView?) = unImpl("StatementVisitor", alterView)
        override fun visit(alter: Alter?) = unImpl("StatementVisitor", alter)
        override fun visit(stmts: Statements?) = unImpl("StatementVisitor", stmts)
        override fun visit(execute: Execute?) = unImpl("StatementVisitor", execute)
        override fun visit(set: SetStatement?) = unImpl("StatementVisitor", set)
        override fun visit(showColumns: ShowColumnsStatement?) = unImpl("StatementVisitor", showColumns)
        override fun visit(merge: Merge?) = unImpl("StatementVisitor", merge)
        override fun visit(select: Select) {
            select.selectBody?.accept(vztSelect)
        }

        override fun visit(upsert: Upsert?) = unImpl("StatementVisitor", upsert)
        override fun visit(use: UseStatement?) = unImpl("StatementVisitor", use)
        override fun visit(block: Block?) = unImpl("StatementVisitor", block)
        override fun visit(values: ValuesStatement?) = unImpl("StatementVisitor", values)
        override fun visit(describe: DescribeStatement?) = unImpl("StatementVisitor", describe)
        override fun visit(explain: ExplainStatement?) = unImpl("StatementVisitor", explain)
        override fun visit(show: ShowStatement?) = unImpl("StatementVisitor", show)
        override fun visit(declare: DeclareStatement?) = unImpl("StatementVisitor", declare)
        override fun visit(grant: Grant?) = unImpl("StatementVisitor", grant)
        override fun visit(createSequence: CreateSequence?) = unImpl("StatementVisitor", createSequence)
        override fun visit(alterSequence: AlterSequence?) = unImpl("StatementVisitor", alterSequence)
        override fun visit(createFunctionalStatement: CreateFunctionalStatement?) =
            unImpl("StatementVisitor", createFunctionalStatement)
    }

    private val vztSelect = object : SelectVisitor {
        override fun visit(plainSelect: PlainSelect) {
            buff.append("\nselect(")
            plainSelect.selectItems?.let {
                var cn = 1
                for (item in it) {
                    item.accept(vztSelectItem)
                    if (cn++ % 5 == 0) buff.append("\n")
                }
            }
            buff.append(")")
            plainSelect.fromItem?.let {
                buff.append("\n.from(")
                it.accept(vztFromItem)
                buff.append(")")
            }

            plainSelect.joins?.let {
                for (join in it) {
                    vztJoin(join)
                }
            }
            plainSelect.where?.let {
                buff.append("\n.where(")
                it.accept(vztExpression)
                buff.append(")")
            }
            plainSelect.groupBy?.accept(vztGroupBy)

            plainSelect.orderByElements?.let {
                buff.append("\n.orderBy(")
                for (ele in it) {
                    ele.accept(vztOrderBy)
                }
                buff.append(")")
            }
            plainSelect.offset?.let {
                buff.append("\n.offset(${it.offset})")
            }
            plainSelect.limit?.let {
                it.offset?.let { of ->
                    buff.append("\n.offset(")
                    of.accept(vztExpression)
                    buff.append(")")
                }
                it.rowCount?.let { of ->
                    buff.append("\n.limit(")
                    of.accept(vztExpression)
                    buff.append(")")
                }
            }
        }

        override fun visit(setOpList: SetOperationList?) = unImpl("SelectVisitor", setOpList)
        override fun visit(withItem: WithItem?) = unImpl("SelectVisitor", withItem)
        override fun visit(valuesStatement: ValuesStatement?) = unImpl("SelectVisitor", valuesStatement)
    }

    private val vztSelectItem = object : SelectItemVisitor {
        override fun visit(allColumns: AllColumns?) = ignore()
        override fun visit(allTableColumns: AllTableColumns) {
            allTableColumns.table.accept(vztFromItem)
            buff.append(", ")
        }

        override fun visit(selectExpressionItem: SelectExpressionItem) {
            selectExpressionItem.expression.accept(vztExpression)
            selectExpressionItem.alias?.let {
                val v = trim(it.name)
                buff.append(""".as("$v")""")
            }
            buff.append(", ")
        }
    }

    private val vztExpression = object : ExpressionVisitor {
        val df19 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val tm10 = DateTimeFormatter.ofPattern("HH:mm:ss")
        override fun visit(bitwiseRightShift: BitwiseRightShift?) = unImpl("ExpressionVisitor", bitwiseRightShift)
        override fun visit(bitwiseLeftShift: BitwiseLeftShift?) = unImpl("ExpressionVisitor", bitwiseLeftShift)
        override fun visit(nullValue: NullValue?) {
            buff.append("null")
        }

        override fun visit(function: net.sf.jsqlparser.expression.Function) {
            warning.add("import static org.jooq.impl.DSL.*;")
            val v = trim(function.name.toLowerCase())
            buff.append("$v(")
            function.parameters.accept(vztItemsList)
            buff.append(")")
        }

        override fun visit(signedExpression: SignedExpression?) = unImpl("ExpressionVisitor", signedExpression)
        override fun visit(jdbcParameter: JdbcParameter?) = unImpl("ExpressionVisitor", jdbcParameter)
        override fun visit(jdbcNamedParameter: JdbcNamedParameter?) = unImpl("ExpressionVisitor", jdbcNamedParameter)
        override fun visit(doubleValue: DoubleValue) {
            buff.append(doubleValue.value.toString())
        }

        override fun visit(longValue: LongValue) {
            buff.append(longValue.value.toString())
        }

        override fun visit(hexValue: HexValue) {
            buff.append(hexValue.value.toString())
        }

        override fun visit(dateValue: DateValue) {
            buff.append("\"" + df19.format(dateValue.value) + "\"")
        }

        override fun visit(timeValue: TimeValue) {
            buff.append("\"" + timeValue.value.toLocalTime().format(tm10) + "\"")
        }

        override fun visit(timestampValue: TimestampValue) {
            buff.append("\"" + df19.format(timestampValue.value) + "\"")
        }

        override fun visit(parenthesis: Parenthesis) {
            val expr = parenthesis.expression
            if (expr is Parenthesis) {
                expr.accept(this)
            } else {
                buff.append("(")
                expr.accept(this)
                buff.append(")")
            }
        }

        override fun visit(stringValue: StringValue) {
            buff.append("\"" + stringValue.value + "\"")
        }

        override fun visit(addition: Addition) {
            addition.leftExpression.accept(this)
            buff.append(".add(")
            addition.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(division: Division) {
            division.leftExpression.accept(this)
            buff.append(".div(")
            division.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(integerDivision: IntegerDivision) {
            integerDivision.leftExpression.accept(this)
            buff.append(".div(")
            integerDivision.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(multiplication: Multiplication) {
            multiplication.leftExpression.accept(this)
            buff.append(".mul(")
            multiplication.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(subtraction: Subtraction) {
            subtraction.leftExpression.accept(this)
            buff.append(".sub(")
            subtraction.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(andExpression: AndExpression) {
            andExpression.leftExpression.accept(this)
            buff.append(".and(")
            andExpression.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(orExpression: OrExpression) {
            orExpression.leftExpression.accept(this)
            buff.append(".or(")
            orExpression.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(between: Between) {
            between.leftExpression.accept(this)
            val st = if (between.isNot) ".notBetween(" else ".between("
            buff.append(st)
            between.betweenExpressionStart.accept(this)
            between.betweenExpressionEnd.accept(this)
            buff.append(")")

        }

        override fun visit(equalsTo: EqualsTo) {
            equalsTo.leftExpression.accept(this)
            buff.append(".eq(")
            equalsTo.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(greaterThan: GreaterThan) {
            greaterThan.leftExpression.accept(this)
            buff.append(".gt(")
            greaterThan.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(greaterThanEquals: GreaterThanEquals) {
            greaterThanEquals.leftExpression.accept(this)
            buff.append(".ge(")
            greaterThanEquals.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(inExpression: InExpression) {
            inExpression.leftExpression.accept(this)
            buff.append(".in(")
            inExpression.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(fullTextSearch: FullTextSearch) = unImpl("ExpressionVisitor", fullTextSearch)
        override fun visit(isNullExpression: IsNullExpression) {
            isNullExpression.leftExpression.accept(this)
            val st = if (isNullExpression.isNot) ".isNotNull()" else ".isNull()"
            buff.append(st)
        }

        override fun visit(isBooleanExpression: IsBooleanExpression?) =
            unImpl("ExpressionVisitor", isBooleanExpression)

        override fun visit(likeExpression: LikeExpression) {
            likeExpression.leftExpression.accept(this)
            val st = if (likeExpression.isCaseInsensitive) ".likeIgnoreCase(" else ".like("
            buff.append(st)
            likeExpression.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(minorThan: MinorThan) {
            minorThan.leftExpression.accept(this)
            buff.append(".lt(")
            minorThan.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(minorThanEquals: MinorThanEquals) {
            minorThanEquals.leftExpression.accept(this)
            buff.append(".le(")
            minorThanEquals.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(notEqualsTo: NotEqualsTo) {
            notEqualsTo.leftExpression.accept(this)
            buff.append(".ne(")
            notEqualsTo.rightExpression.accept(this)
            buff.append(")")
        }

        override fun visit(column: Column) {
            val ref = refTab(column.table, false)
            val cl = colName(trim(column.columnName))
            buff.append("$ref.$cl")
        }

        override fun visit(subSelect: SubSelect) = vztSubSelect(subSelect)
        override fun visit(caseExpression: CaseExpression) {
            caseExpression.switchExpression?.accept(this)
            caseExpression.whenClauses?.let {
                warning.add("import static org.jooq.impl.DSL.*;")
                for (whenClause in it) {
                    buff.append("when(")
                    whenClause.whenExpression.accept(this)
                    buff.append(",")
                    whenClause.thenExpression.accept(this)
                    buff.append(")")
                }
            }
            caseExpression.elseExpression?.let {
                buff.append(".otherwise(")
                it.accept(this)
                buff.append(")")
            }
        }

        override fun visit(whenClause: WhenClause?) = unImpl("ExpressionVisitor", whenClause)
        override fun visit(existsExpression: ExistsExpression?) = unImpl("ExpressionVisitor", existsExpression)
        override fun visit(allComparisonExpression: AllComparisonExpression?) =
            unImpl("ExpressionVisitor", allComparisonExpression)

        override fun visit(anyComparisonExpression: AnyComparisonExpression?) =
            unImpl("ExpressionVisitor", anyComparisonExpression)

        override fun visit(concat: Concat?) = unImpl("ExpressionVisitor", concat)
        override fun visit(matches: Matches?) = unImpl("ExpressionVisitor", matches)
        override fun visit(bitwiseAnd: BitwiseAnd?) = unImpl("ExpressionVisitor", bitwiseAnd)
        override fun visit(bitwiseOr: BitwiseOr?) = unImpl("ExpressionVisitor", bitwiseOr)
        override fun visit(bitwiseXor: BitwiseXor?) = unImpl("ExpressionVisitor", bitwiseXor)
        override fun visit(castExpression: CastExpression?) = unImpl("ExpressionVisitor", castExpression)
        override fun visit(modulo: Modulo?) = unImpl("ExpressionVisitor", modulo)
        override fun visit(analyticExpression: AnalyticExpression?) = unImpl("ExpressionVisitor", analyticExpression)
        override fun visit(extractExpression: ExtractExpression?) = unImpl("ExpressionVisitor", extractExpression)
        override fun visit(intervalExpression: IntervalExpression?) = unImpl("ExpressionVisitor", intervalExpression)
        override fun visit(oracleHierarchicalExpression: OracleHierarchicalExpression?) =
            unImpl("ExpressionVisitor", oracleHierarchicalExpression)

        override fun visit(regExpMatchOperator: RegExpMatchOperator?) =
            unImpl("ExpressionVisitor", regExpMatchOperator)

        override fun visit(jsonExpression: JsonExpression?) = unImpl("ExpressionVisitor", jsonExpression)
        override fun visit(jsonOperator: JsonOperator?) = unImpl("ExpressionVisitor", jsonOperator)
        override fun visit(regExpMySQLOperator: RegExpMySQLOperator?) =
            unImpl("ExpressionVisitor", regExpMySQLOperator)

        override fun visit(userVariable: UserVariable?) = unImpl("ExpressionVisitor", userVariable)
        override fun visit(numericBind: NumericBind?) = unImpl("ExpressionVisitor", numericBind)
        override fun visit(keepExpression: KeepExpression?) = unImpl("ExpressionVisitor", keepExpression)
        override fun visit(mySQLGroupConcat: MySQLGroupConcat) {
            warning.add("import static org.jooq.impl.DSL.*;")
            // groupConcat(BOOK.ID).orderBy(BOOK.ID).separator("; "))
            buff.append("groupConcat(")
            if (mySQLGroupConcat.isDistinct) {
                buff.append("distinct(")
            }
            mySQLGroupConcat.expressionList?.accept(vztItemsList)
            if (mySQLGroupConcat.isDistinct) {
                buff.append(")")
            }
            buff.append(")")
            mySQLGroupConcat.orderByElements?.let {
                buff.append(".orderBy(")
                for (element in it) {
                    element.accept(vztOrderBy)
                    buff.append(",")
                }
                buff.append(")")
            }
        }

        override fun visit(valueListExpression: ValueListExpression?) =
            unImpl("ExpressionVisitor", valueListExpression)

        override fun visit(rowConstructor: RowConstructor?) = unImpl("ExpressionVisitor", rowConstructor)
        override fun visit(oracleHint: OracleHint?) = unImpl("ExpressionVisitor", oracleHint)
        override fun visit(timeKeyExpression: TimeKeyExpression?) = unImpl("ExpressionVisitor", timeKeyExpression)
        override fun visit(dateTimeLiteralExpression: DateTimeLiteralExpression?) =
            unImpl("ExpressionVisitor", dateTimeLiteralExpression)

        override fun visit(notExpression: NotExpression?) = unImpl("ExpressionVisitor", notExpression)
        override fun visit(nextValExpression: NextValExpression?) = unImpl("ExpressionVisitor", nextValExpression)
        override fun visit(collateExpression: CollateExpression?) = unImpl("ExpressionVisitor", collateExpression)
        override fun visit(similarToExpression: SimilarToExpression?) =
            unImpl("ExpressionVisitor", similarToExpression)

        override fun visit(arrayExpression: ArrayExpression?) = unImpl("ExpressionVisitor", arrayExpression)
    }

    private fun vztSubSelect(subSelect: SubSelect) {
        warning.add("[SubSelect should Derived table](https://www.jooq.org/doc/latest/manual/sql-building/table-expressions/nested-selects/)")
        subSelect.selectBody.accept(vztSelect)
        subSelect.alias?.let {
            val v = trim(it.name)
            buff.append(""".asTable("$v")""")
        }
    }

    private val vztFromItem = object : FromItemVisitor {
        override fun visit(tableName: Table) {
            buff.append(refTab(tableName, true))
        }

        override fun visit(subSelect: SubSelect) = vztSubSelect(subSelect)
        override fun visit(subjoin: SubJoin) {
            subjoin.left.accept(this)
            subjoin.joinList?.forEach { vztJoin(it) }
        }

        override fun visit(lateralSubSelect: LateralSubSelect?) = unImpl("FromItemVisitor", lateralSubSelect)
        override fun visit(valuesList: ValuesList?) = unImpl("FromItemVisitor", valuesList)
        override fun visit(tableFunction: TableFunction?) = unImpl("FromItemVisitor", tableFunction)
        override fun visit(parenthesisFromItem: ParenthesisFromItem?) = unImpl("FromItemVisitor", parenthesisFromItem)
    }

    private val vztItemsList: ItemsListVisitor = object : ItemsListVisitor {
        override fun visit(subSelect: SubSelect) = vztSubSelect(subSelect)
        override fun visit(expressionList: ExpressionList) {
            expressionList.expressions?.forEach {
                it.accept(vztExpression)
                buff.append(", ")
            }
        }

        override fun visit(namedExpressionList: NamedExpressionList?) =
            unImpl("ItemsListVisitor", namedExpressionList)

        override fun visit(multiExprList: MultiExpressionList) {
            multiExprList.exprList?.forEach {
                buff.append("\n.values(")
                it.accept(this)
                buff.append(")")
            }
        }
    }

    private val vztOrderBy: OrderByVisitor = OrderByVisitor { orderBy ->
        orderBy.expression.accept(vztExpression)
        if (orderBy.isAscDescPresent) {
            if (orderBy.isAsc) {
                buff.append(".asc()")
            } else {
                buff.append(".desc()")
            }
        }
        buff.append(", ")
    }

    private val vztGroupBy: GroupByVisitor = GroupByVisitor { groupBy ->
        groupBy.groupByExpressions?.let {
            buff.append("\n.groupBy(")
            for (expr in it) {
                expr.accept(vztExpression)
                buff.append(", ")
            }
            buff.append(")")
        }
    }
}