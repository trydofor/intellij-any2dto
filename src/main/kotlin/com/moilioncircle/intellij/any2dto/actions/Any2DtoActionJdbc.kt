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
import com.intellij.psi.PsiElement
import com.moilioncircle.intellij.any2dto.helper.IdeaUiHelper
import com.moilioncircle.intellij.any2dto.helper.MergerHelper
import com.moilioncircle.intellij.any2dto.helper.MergerHelper.ColumnInfo
import com.moilioncircle.intellij.any2dto.settings.SettingsState


class Any2DtoActionJdbc : AnAction() {
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
            IdeaUiHelper.showError("failed to generate by jdbc", t)
        }
    }

    override fun update(e: AnActionEvent) {
        // Sql Query Result
        val dataGrid = e.getData(DatabaseDataKeys.DATA_GRID_KEY)
        if (dataGrid != null) {
            e.presentation.isEnabled = true
            return
        }

        // database tool window
        val psiElements = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY)
        if (psiElements.isNullOrEmpty()) {
            e.presentation.isEnabled = false
            return
        }

        // select table or column
        val element = psiElements[0]
        e.presentation.isEnabled = element is DbTable || element is DbColumn
    }

    // /////////
    private fun byResult(dataGrid: DataGrid, e: AnActionEvent) {
        val grdCol = dataGrid.getDataModel(DataAccessType.DATABASE_DATA).columns
        val sqlCol = ArrayList<ColumnInfo>()

        for (column in grdCol) {
            sqlCol.add(ColumnInfo(column.name, column.typeName, column.precision, column.scale))
        }
        mergeJava(sqlCol, e)
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
            }
        }
        mergeJava(sqlCol, e)
    }

    private fun mergeJava(sqlCol: List<ColumnInfo>, e: AnActionEvent) {
        val state = SettingsState.loadSettingState()
        val project = e.getData(LangDataKeys.PROJECT)
        val fields = MergerHelper.matchFields(state, sqlCol)
        MergerHelper.generateJava(state, fields, project)
    }
}