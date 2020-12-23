package com.moilioncircle.intellij.any2dto.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.moilioncircle.intellij.any2dto.helper.IdeaUiHelper


class Any2DtoAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val action = when {
            IdeaUiHelper.jooqAccept(e) -> "MoilionCircle.Any2dto.Jooq"
            IdeaUiHelper.jdbcAccept(e) -> "MoilionCircle.Any2dto.Jdbc"
            else -> "MoilionCircle.Any2dto.Text"
        }

        ActionManager.getInstance().getAction(action).actionPerformed(e)
    }
}