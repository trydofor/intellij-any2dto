package com.moilioncircle.intellij.any2dto.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import com.moilioncircle.intellij.any2dto.helper.IdeaUiHelper
import com.moilioncircle.intellij.any2dto.helper.MergerHelper
import com.moilioncircle.intellij.any2dto.helper.MergerHelper.FieldInfo
import com.moilioncircle.intellij.any2dto.settings.SettingsState

class Any2DtoActionText : AnAction() {
    private val logger = Logger.getInstance(Any2DtoActionText::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        try {
            val editor = e.getData(CommonDataKeys.EDITOR) ?: return
            val select = editor.caretModel.primaryCaret.selectedText ?: return
            val state = SettingsState.loadSettingState()
            val wsp = state.textWordSeparator.toRegex()
            var lines = select.split(state.textLineSeparator.toRegex()).filter { it.isNotBlank() }
            while (state.textLinePrompt && lines.size == 1) {
                val reg =
                    Messages.showInputDialog("only one line, may need new Separator",
                        "Input line Separator Regexp",
                        Messages.getQuestionIcon(),
                        state.textLineSeparator, null)
                if (reg.isNullOrEmpty() || reg == state.textLineSeparator) {
                    break
                } else {
                    lines = select.split(reg.toRegex()).filter { it.isNotBlank() }
                }
            }

            val fields = lines.map {
                val wds = it.split(wsp).filter { t -> t.isNotBlank() }
                when (val sz = wds.size) {
                    0 -> FieldInfo("", "")
                    1 -> FieldInfo(wds[0], "")
                    else -> FieldInfo(wds[sz - 1], wds[sz - 2])
                }
            }.filter { it.name.isNotBlank() }

            val project = e.getData(LangDataKeys.PROJECT)
            MergerHelper.generateJava(state, fields, project, "Select Text")
        } catch (t: Throwable) {
            logger.error("failed to generate by text", t)
            IdeaUiHelper.showError("failed to generate by jooq", t)
        }
    }
}
