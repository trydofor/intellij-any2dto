package com.moilioncircle.intellij.any2dto.helper

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.moilioncircle.intellij.any2dto.helper.ConfigHelper.TypeMapping
import com.moilioncircle.intellij.any2dto.settings.SettingsState
import pro.fessional.meepo.sack.Parser
import java.awt.datatransfer.StringSelection
import java.io.File

/**
 * @author trydofor
 * @since 2020-12-16
 */
object MergerHelper {

    data class ColumnInfo(
        val name: String,
        val type: String,
        val precision: Int,
        val scale: Int,
    )

    data class FieldInfo(
        val name: String,
        val type: String,
    )

    fun matchMapping(col: ColumnInfo, rule: TypeMapping): Boolean {
        val sqlType = rule.sqlType
        if (sqlType == "*") return true
        if (!sqlType.equals(col.type, true)) return false
        if (rule.sqlPrecisionBgn > 0 && col.precision < rule.sqlPrecisionBgn) return false
        if (rule.sqlPrecisionEnd > 0 && col.precision > rule.sqlPrecisionEnd) return false
        if (rule.sqlScaleBgn > 0 && col.scale < rule.sqlScaleBgn) return false
        if (rule.sqlScaleEnd > 0 && col.scale > rule.sqlScaleEnd) return false
        return true
    }

    fun matchMapping(col: ColumnInfo, rules: List<TypeMapping>): FieldInfo {
        val javaName = NamingFuns.camelCase(col.name)
        val javaType = rules.find { matchMapping(col, it) }?.javaType ?: "Unknown"
        return FieldInfo(javaName, javaType)
    }

    fun matchFields(state: SettingsState, cols: List<ColumnInfo>): List<FieldInfo> {
        val rules = state.javaTypeMapping.lines()
            .map { ConfigHelper.parseMapping(it) }
            .filter { it.javaType.isNotEmpty() }
        return cols.map { matchMapping(it, rules) }.filter { it.name.isNotEmpty() }
    }

    fun mergeFields(state: SettingsState, fields: List<FieldInfo>, name: String): String {
        val javaTypeImports = fields.filter { it.type.contains('.') }.map { it.type }.toSet()
        val context = HashMap<String, Any>()
        context["javaPackageName"] = state.javaPackageName
        context["javaTypeImports"] = javaTypeImports
        context["className"] = name
        context["javaFields"] = fields

        val template = if (state.usingInnerClass) state.javaTempletInner else state.javaTempletOuter
        val gene = Parser.parse(template)
        return gene.merge(context)
    }

    fun generateJava(state: SettingsState, fields: List<FieldInfo>, project: Project?, from: String) {
        val defaultName = state.javaDtoName
        if (state.usingClipboard) {
            val dtoName = if (state.javaDtoPrompt) {
                Messages.showInputDialog(null,
                    """have ${fields.size} fields
               --
               cancel to use `$defaultName` as class name""".trimIndent(),
                    "need DTO class name by $from",
                    Messages.getQuestionIcon(),
                    defaultName, null) ?: defaultName
            } else {
                defaultName
            }
            val javaCode = mergeFields(state, fields, dtoName)
            copyClipboard(javaCode, "")
        } else {
            val dtoName = if (state.javaDtoPrompt) {
                Messages.showInputDialog(null,
                    """use inner template = ${state.usingInnerClass}
               java source path   = ${state.javaSourcePath}
               java package name  = ${state.javaPackageName}
               --
               have ${fields.size} fields
               --
               cancel to use `$defaultName` as class name""".trimIndent(),
                    "need DTO class name by $from",
                    Messages.getQuestionIcon(),
                    defaultName, null) ?: defaultName
            } else {
                defaultName
            }

            val javaCode = mergeFields(state, fields, dtoName)

            if (project == null) {
                copyClipboard(javaCode, "current project not found, copy to clipboard")
                return
            }

            val src = if (state.javaSourcePath.startsWith("/")) {
                File(state.javaSourcePath)
            } else {
                File(project.basePath, state.javaSourcePath)
            }
            val pkg = File(src, state.javaPackageName.replace('.', '/'))
            if (!pkg.exists()) {
                pkg.mkdirs()
            }

            val vpg = VfsUtil.findFileByIoFile(pkg, true)
            if (vpg == null) {
                copyClipboard(javaCode, "current source path not found, copy to clipboard")
                return
            }
            var dtoJava = vpg.findChild("$dtoName.java")
            if (dtoJava == null) {
                dtoJava = vpg.createChildData(this, "$dtoName.java")
            } else {
                val yn = Messages.showYesNoDialog("file existed, [Yes] to overwrite, else to clipboard",
                    "File overwrite",
                    Messages.getQuestionIcon())
                if (yn != Messages.YES) {
                    copyClipboard(javaCode, "")
                    return
                }
            }
            dtoJava.setBinaryContent(javaCode.toByteArray())

            // optimize
            WriteCommandAction.runWriteCommandAction(project) {
                // format
                val psiManager = PsiManager.getInstance(project)
                val psiJava = psiManager.findFile(dtoJava) as PsiJavaFile
                CodeStyleManager.getInstance(project).reformat(psiJava)
                val codeStyle = JavaCodeStyleManager.getInstance(project)
                codeStyle.optimizeImports(psiJava)
                codeStyle.shortenClassReferences(psiJava)
                //
                val descriptor = OpenFileDescriptor(project, dtoJava)
                FileEditorManager.getInstance(project).openEditor(descriptor, true)
            }
        }
    }

    private fun copyClipboard(code: String, error: String) {
        if (error.isNotEmpty()) {
            Messages.showMessageDialog(error, "Notice", Messages.getWarningIcon())
        }
        CopyPasteManager.getInstance().setContents(StringSelection(code))
    }
}
