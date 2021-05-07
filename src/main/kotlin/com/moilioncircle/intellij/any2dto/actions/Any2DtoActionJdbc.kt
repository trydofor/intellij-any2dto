package com.moilioncircle.intellij.any2dto.actions

import com.intellij.database.DatabaseDataKeys
import com.intellij.database.datagrid.DataGrid
import com.intellij.database.model.basic.BasicLikeTable
import com.intellij.database.psi.DbColumn
import com.intellij.database.psi.DbTable
import com.intellij.database.run.ui.DataAccessType
import com.intellij.database.util.DbUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.moilioncircle.intellij.any2dto.helper.IdeaUiHelper
import com.moilioncircle.intellij.any2dto.helper.MergerHelper
import com.moilioncircle.intellij.any2dto.helper.MergerHelper.ColumnInfo
import com.moilioncircle.intellij.any2dto.settings.SettingsState

class Any2DtoActionJdbc : AnAction() {
    private val logger = Logger.getInstance(Any2DtoActionJdbc::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        try {
            val dataGrid = e.getData(DatabaseDataKeys.DATA_GRID_KEY)
            if (dataGrid != null) {
                byResult(dataGrid, e)
                return
            }

            val psiElements = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY)
            if (!psiElements.isNullOrEmpty()) {
                byColumn(psiElements, e)
                return
            }
        } catch (t: Throwable) {
            logger.error("failed to generate by jdbc", t)
            IdeaUiHelper.showError("failed to generate by jdbc", t)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = IdeaUiHelper.jdbcAccept(e)
    }

    // /////////
    private fun byResult(dataGrid: DataGrid, e: AnActionEvent) {
        val grdCol = dataGrid.getDataModel(DataAccessType.DATABASE_DATA).columns
        val sqlCol = ArrayList<ColumnInfo>()

        for (column in grdCol) {
            sqlCol.add(ColumnInfo(column.name, column.typeName, column.precision, column.scale))
        }
        mergeJava(sqlCol, e, "Query Result")
    }

    private fun byColumn(elements: Array<PsiElement>, e: AnActionEvent) {
        val sqlCol = ArrayList<ColumnInfo>()
        for (ele in elements) {
            if (ele is DbTable) {
                val dasObject = DbUtil.getDasObject(ele) as BasicLikeTable
                for (col in dasObject.columns) {
                    val dt = col.dataType
                    val el = ColumnInfo(col.name, dt.typeName, dt.precision, dt.scale)
                    sqlCol.add(el)
                }
            } else if (ele is DbColumn) {
                val dt = ele.dataType
                val el = ColumnInfo(ele.name, dt.typeName, dt.precision, dt.scale)
                sqlCol.add(el)
            } else {
                logger.warn("unsupported type " + ele.text)
            }
        }
        mergeJava(sqlCol, e, "Table/Columns")
    }

    private fun mergeJava(sqlCol: List<ColumnInfo>, e: AnActionEvent, from: String) {
        val state = SettingsState.loadSettingState()
        val project = e.getData(LangDataKeys.PROJECT)
        val fields = MergerHelper.matchFields(state, sqlCol)
        MergerHelper.generateJava(state, fields, project, from)
    }
}
