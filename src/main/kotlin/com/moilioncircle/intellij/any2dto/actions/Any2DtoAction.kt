package com.moilioncircle.intellij.any2dto.actions

import com.intellij.database.DatabaseDataKeys
import com.intellij.database.console.JdbcConsole
import com.intellij.database.psi.DbDataSource
import com.intellij.database.run.ui.DataAccessType
import com.intellij.database.util.DbUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.util.containers.JBIterable


class Any2DtoAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val message = StringBuilder()
        val project = e.getData(LangDataKeys.PROJECT)
        var dataSources: JBIterable<DbDataSource>
        if (project != null) {
            dataSources = DbUtil.getDataSources(project)
            for (ds in dataSources) {
                message.append("datasource = $ds.name\n")
            }
        }



//        BrowserUtil.browse("https://stackoverflow.com/questions/ask")

        val dataGrid = e.getData(DatabaseDataKeys.DATA_GRID_KEY)
        val visibleColumns = dataGrid?.getDataModel(DataAccessType.DATABASE_DATA);


        val psiElements = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY)
        val selectItems = e.getData(LangDataKeys.SELECTED_ITEMS)
//        DasUtil.getColumns()
        val editor = e.getData(LangDataKeys.EDITOR)
        var caretModel: CaretModel
        var start = 0
        if (editor != null) {
            caretModel = editor.caretModel
            val caret = caretModel.currentCaret
            val selectedText = caret.selectedText
            start = caret.selectionStart
        }

        val psiFile = e.getData(LangDataKeys.PSI_FILE)
        var element: PsiElement?
        var elementType: IElementType?
        if (psiFile != null) {
            element = psiFile.findElementAt(start)
            elementType = element.elementType
            val parent = element?.parent
        }
//        PsiTreeUtil

        val jdbcConsole = JdbcConsole.findConsole(e)

        Messages.showMessageDialog(project, message.toString(), "Greeting", Messages.getInformationIcon())
    }
}