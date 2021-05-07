package com.moilioncircle.intellij.any2dto.helper

import com.intellij.database.DatabaseDataKeys
import com.intellij.database.psi.DbColumn
import com.intellij.database.psi.DbTable
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiJavaFile
import com.intellij.ui.EditorTextField
import java.io.PrintWriter
import java.io.StringWriter

/**
 * @author trydofor
 * @since 2020-12-21
 */
object IdeaUiHelper {

    fun showText(msg: String, text: String) {
        Messages.showMultilineInputDialog(null,
            msg,
            "Notice",
            text,
            Messages.getInformationIcon(),
            null)
    }

    fun showError(msg: String, t: Throwable) {
        val out = StringWriter()
        val p = PrintWriter(out)
        t.printStackTrace(p)
        Messages.showMultilineInputDialog(null,
            msg,
            "Notice",
            out.toString(),
            Messages.getErrorIcon(),
            null)
    }

    fun jdbcAccept(e: AnActionEvent): Boolean {
        // Sql Query Result
        val dataGrid = e.getData(DatabaseDataKeys.DATA_GRID_KEY)
        if (dataGrid != null) return true

        // database tool window
        val selected = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY)
        if (selected.isNullOrEmpty()) return false

        // select table or column
        val element = selected[0]
        return element is DbTable || element is DbColumn
    }

    fun jooqAccept(e: AnActionEvent): Boolean {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        return psiFile is PsiJavaFile
    }

    @JvmStatic
    fun createMappingEditor(text: String, project: Project): EditorTextField {
        val fileType = try {
            // somethings hehe, can see but can not use it
            val propType = Class.forName("com.intellij.lang.properties.PropertiesFileType") as Class<*>
            val field = propType.getField("INSTANCE")
            field[null] as FileType
        } catch (e: Exception) {
            PlainTextFileType.INSTANCE
        }
        return createEditor(text, project, fileType)
    }

    @JvmStatic
    fun createJavaEditor(text: String, project: Project) = createEditor(text, project, JavaFileType.INSTANCE)

    fun createEditor(text: String, project: Project, fileType: FileType): EditorTextField {
        // inal Project project = ProjectUtil.guessCurrentProject(getMainFormPanel());
        val editor = EditorTextField(text, project, fileType)
        editor.setOneLineMode(false)
        return editor
    }
}
