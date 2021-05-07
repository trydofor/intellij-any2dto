package com.moilioncircle.intellij.any2dto.sqldsl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author trydofor
 * @since 2021-01-01
 */
class SqlVisitorsTest {

    private val sqlVzt = SqlVisitors("{col|BIG_SNAKE}")
    private val tr = "\\s+".toRegex()
    private fun eq(
        selectSql: String,
        refTabs: Set<Triple<String, String, Boolean>>,
        jooqDsl: String,
    ) {
        sqlVzt.visitSql(selectSql)
        val (jooqGen, refGen) = sqlVzt.jooqDsl()
        assertEquals(tr.replace(jooqDsl, ""), tr.replace(jooqGen, ""))
        assertEquals(refTabs.sortedBy { it.first }, refGen.sortedBy { it.first })
        assertTrue(sqlVzt.unImpls.isEmpty())
    }

    @Test
    fun test01() = eq(
        """
            select * 
            from sys_schema_journal
        """,
        setOf(Triple("t1", "sys_schema_journal", false)),
        """
                select()
                .from(t1)
            """
    )

    @Test
    fun test02() = eq(
        """
            select 
            t.id, t.create_dt
            from sys_schema_journal as t
        """,
        setOf(Triple("t", "sys_schema_journal", true)),
        """
                select(t.ID, t.CREATE_DT)
                .from(t)
            """
    )

    @Test
    fun test03() = eq(
        """
            select 
            id, t.create_dt
            from sys_schema_journal t
        """,
        setOf(Triple("t", "sys_schema_journal", true)),
        """
                select(t.ID, t.CREATE_DT)
                .from(t)
            """
    )

    @Test
    fun test04() = eq(
        """
            select 
            id, t.create_dt dt
            from sys_schema_journal t
            order by id
        """,
        setOf(Triple("t", "sys_schema_journal", true)),
        """
                select(t.ID, t.CREATE_DT.as("dt"))
                .from(t)
                .orderBy(t.ID)
            """
    )

    // table alias can not impl right， should use Derived table
    // https://www.jooq.org/doc/latest/manual/sql-building/table-expressions/nested-selects/
    @Test
    fun test11() = eq(
        """
            select 
            t.create_dt 
            from (select id,create_dt from sys_schema_journal) as t
            where t.id >= 19
        """,
        setOf(Triple("t1", "sys_schema_journal", false),
            Triple("t", "t", false)),
        """
                select(t.CREATE_DT)
                .from(
                select(t1.ID,t1.CREATE_DT).from(t1).asTable("t")
                ).where(t.ID.ge(19))
            """
    )

    // 复杂sql不建议使用jooq，使用jdbc template
    // 不保证正确，只是个人观察
    @Test
    fun test21() =eq("""select 
                t1.id P_ID, 
                t1.ID O_ID, 
                t2.NAME O_NAME, 
                t3.num NUM, 
                t1.r_id R_ID, 
                t1.CON CON, 
                t1.`STATUS`, 
                t4.ffn FFN, 
                t4.ggw GGW, 
                t1.PCY PCY, 
                t1.way WAY, 
                ifnull(`t9`.`ttl`,`t6`.`ttl`) AS `tt_tl`, 
                (case when (`t8`.`ddv` > 0) then `t7`.`ccoo` else NULL end) AS `ccoo`, 
                (case when (`t8`.`ddv` > 0) then `t7`.`ttll` else NULL end) AS `ttll`, 
                t5.msg MSG 
                from table1 as t1 
                join table3 t3  
                on t1.O_ID = t3.O_ID  
                and t1.ID = t3.i_id 
                and t3.status <> 'BAD'  
                and t1.TTYP = 'Some' 
                join t5  
                on t1.id = t5.t_id 
                join t6 
                on t6.p_id = t3.id 
                join table2 t2  
                on t1.O_ID = t2.ID 
                join t7  
                on t4.ID = t7.O_ID  
                and t4.lcc = t7.lcc 
                join t8  
                on t1.r_id = t8.r_id  
                and t8.success = true 
                left join t9  
                on t1.id = t9.v_id  
                and t4.otype = 'Red' 
                left join t4  
                on t2.O_ID = t4.O_ID  
                and t1.email = t4.email 
                where t1.O_ID = 789 
                and t6.l_id is not null 
                group By t2.r_id 
                order By t1.coo desc, t6.ID asc, t3.num desc, t5.way asc 
                limit 2, 10
                """,
        setOf(
            Triple("t1", "table1", true),
            Triple("t2", "table2", true),
            Triple("t3", "table3", true),
            Triple("t4", "t4", false),
            Triple("t5", "t5", false),
            Triple("t6", "t6", false),
            Triple("t7", "t7", false),
            Triple("t8", "t8", false),
            Triple("t9", "t9", false),
        ),
        """
            select(
            t1.ID.as("P_ID"),
            t1.ID.as("O_ID"),
            t2.NAME.as("O_NAME"),
            t3.NUM.as("NUM"),
            t1.R_ID.as("R_ID"),
            t1.CON.as("CON"),
            t1.STATUS,
            t4.FFN.as("FFN"),
            t4.GGW.as("GGW"),
            t1.PCY.as("PCY"),
            t1.WAY.as("WAY"),
            ifnull(t9.TTL,t6.TTL).as("tt_tl"),
            (when((t8.DDV.gt(0)),t7.CCOO).otherwise(null)).as("ccoo"),
            (when((t8.DDV.gt(0)),t7.TTLL).otherwise(null)).as("ttll"),
            t5.MSG.as("MSG")
            )
            .from(t1)
            .join(t3).on(t1.O_ID.eq(t3.O_ID).and(t1.ID.eq(t3.I_ID)).and(t3.STATUS.ne("BAD")).and(t1.TTYP.eq("Some")))
            .join(t5).on(t1.ID.eq(t5.T_ID))
            .join(t6).on(t6.P_ID.eq(t3.ID))
            .join(t2).on(t1.O_ID.eq(t2.ID))
            .join(t7).on(t4.ID.eq(t7.O_ID).and(t4.LCC.eq(t7.LCC)))
            .join(t8).on(t1.R_ID.eq(t8.R_ID).and(t8.SUCCESS.eq(t1.TRUE)))
            .leftOuterJoin(t9).on(t1.ID.eq(t9.V_ID).and(t4.OTYPE.eq("Red")))
            .leftOuterJoin(t4).on(t2.O_ID.eq(t4.O_ID).and(t1.EMAIL.eq(t4.EMAIL)))
            .where(t1.O_ID.eq(789).and(t6.L_ID.isNotNull()))
            .groupBy(t2.R_ID)
            .orderBy(t1.COO.desc(),t6.ID.asc(),t3.NUM.desc(),t5.WAY.asc())
            .offset(2).limit(10)
            """
    )
}
