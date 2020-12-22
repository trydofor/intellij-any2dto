package com.moilioncircle.intellij.any2dto.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.util.elementType


class Any2DtoActionText : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        if (editor == null || psiFile == null) return

        val caretModel = editor.caretModel
        val caret = caretModel.primaryCaret
        val start = caret.selectionStart
        val end = caret.selectionEnd
        val element = psiFile.findElementAt(start)
        element.elementType
//        PsiTreeUtil
    }
}