package com.moilioncircle.intellij.any2dto.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.Messages
import com.moilioncircle.intellij.any2dto.helper.IdeaUiHelper
import com.moilioncircle.intellij.any2dto.settings.SettingsState
import com.moilioncircle.intellij.any2dto.sqldsl.DslMerger
import java.awt.datatransfer.StringSelection


class Any2DtoActionSqlDsl : AnAction() {
    private val logger = Logger.getInstance(Any2DtoActionSqlDsl::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        try {
            val editor = e.getData(CommonDataKeys.EDITOR)
            val code = editor?.caretModel?.primaryCaret?.selectedText?.trim() ?: ""
            if (!code.startsWith("select", true)) {
                Messages.showWarningDialog("only support sql select statement", "Unsupported")
                return
            }

            val state = SettingsState.loadSettingState()
            val merger = DslMerger(state.textSqlColumn, state.textSqlTable, state.textSqlDsl, state.textDslName)
            val jooqDsl = merger.merge(code)

            CopyPasteManager.getInstance().setContents(StringSelection(jooqDsl))
        } catch (t: Throwable) {
            logger.error("failed to create mysql PSI by selected text", t)
            IdeaUiHelper.showError("failed to create mysql PSI by selected text", t)
        }
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor?.caretModel?.primaryCaret?.selectedText?.isNotBlank() ?: false
    }
}