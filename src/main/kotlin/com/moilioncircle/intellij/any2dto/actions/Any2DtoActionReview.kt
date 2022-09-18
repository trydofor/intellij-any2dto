package com.moilioncircle.intellij.any2dto.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
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
            val filePath =
                if (file.canonicalPath != null && project.basePath != null) {
                    file.canonicalPath!!.replace(project.basePath!!, "")
                } else {
                    file.canonicalPath ?: ""
                }
            ctx["ProjName"] = project.name
            ctx["FilePath"] = filePath

            // /Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/rt.jar!/sun/misc/URLClassPath.class
            // /Users/trydofor/.sdkman/candidates/java/11.0.2-open/lib/src.zip!/java.base/java/net/URLClassLoader.java
            // /Users/trydofor/.m2/repository/org/springframework/security/spring-security-core/5.6.3/spring-security-core-5.6.3.jar!/org/springframework/security/core/context/SecurityContextHolder.class
            if (filePath.contains("\\.[^/!]+!/".toRegex())) {
                val lof = filePath.lastIndexOf("!/")
                ctx["FilePath"] = filePath.substring(lof + 1);
                val prt = filePath.substring(0, lof).split("/")
                val ver = "\\d+(\\.\\d+)+".toRegex()
                for (i in prt.size - 1 downTo 1) {
                    if (ver.containsMatchIn(prt[i])) {
                        ctx["ProjName"] = if (ver.containsMatchIn(prt[i - 1])) {
                            prt[i]
                        } else {
                            prt[i - 1] + '/' + prt[i]
                        }
                        break;
                    }
                }
            }

            val instant = Instant.ofEpochMilli(file.timeStamp)
            val lastTime = instant.atZone(ZoneId.systemDefault())
            ctx["ModTime"] = lastTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val projectCountingService = project.getService(GitRevisionService::class.java)
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
