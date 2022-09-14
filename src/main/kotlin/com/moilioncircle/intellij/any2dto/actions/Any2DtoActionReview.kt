package com.moilioncircle.intellij.any2dto.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.moilioncircle.intellij.any2dto.helper.IdeaUiHelper
import com.moilioncircle.intellij.any2dto.helper.MergerHelper.copyClipboard
import com.moilioncircle.intellij.any2dto.services.GitRevisionService
import com.moilioncircle.intellij.any2dto.settings.SettingsState
import pro.fessional.meepo.sack.Parser
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class Any2DtoActionReview : AnAction() {
    private val logger = Logger.getInstance(Any2DtoActionReview::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        try {
            val editor = e.getData(CommonDataKeys.EDITOR) ?: return
            val project = editor.project ?: return
            val caret = editor.caretModel.primaryCaret
            val select = caret.selectedText ?: return
            val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return

            val ctx = HashMap<String, Any>()
            ctx["FileName"] = file.name
            ctx["FileType"] = file.fileType.name.toLowerCase()
            ctx["LineStart"] = caret.logicalPosition.line + 1
            ctx["LineEnd"] = editor.visualToLogicalPosition(caret.selectionEndPosition).line + 1

            ctx["CodeCopy"] = select.trimIndent()
            ctx["Project"] = project.name
            if (file.canonicalPath != null && project.basePath != null) {
                ctx["FilePath"] = file.canonicalPath!!.replace(project.basePath!!, "")
            } else {
                ctx["FilePath"] = file.canonicalPath ?: ""
            }

            val instant = Instant.ofEpochMilli(file.timeStamp)
            val lastTime = instant.atZone(ZoneId.systemDefault())
            ctx["ModTime"] = lastTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val projectCountingService = ApplicationManager.getApplication().getService(GitRevisionService::class.java)
            ctx["GitHash"] = projectCountingService?.getRevision(project, file) ?: ""

            val tmpl = SettingsState.loadSettingState(project).codeTempletReview
            val gene = Parser.parse(tmpl)
            val text = gene.merge(ctx)
            copyClipboard(text, "")
        } catch (t: Throwable) {
            logger.error("failed to generate review", t)
            IdeaUiHelper.showError("failed to generate review", t)
        }
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor?.caretModel?.primaryCaret?.selectedText?.isNotBlank() ?: false
    }
}
