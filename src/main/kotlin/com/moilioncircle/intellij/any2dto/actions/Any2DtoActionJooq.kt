package com.moilioncircle.intellij.any2dto.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.moilioncircle.intellij.any2dto.helper.IdeaUiHelper


class Any2DtoActionJooq : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        try {
            val editor = e.getData(CommonDataKeys.EDITOR)
            val psiJava = e.getData(CommonDataKeys.PSI_FILE)
            if (editor == null || psiJava !is PsiJavaFile) {
                Messages.showWarningDialog("only support java source file", "Unsupported")
                return
            }

            val caret = editor.caretModel.primaryCaret
            val eleBgn = psiJava.findElementAt(caret.selectionStart)
            val eleEnd = psiJava.findElementAt(caret.selectionEnd - 1)
            if (eleBgn == null || eleEnd == null) {
                Messages.showWarningDialog("only Jooq's field-like expression list", "Unsupported")
                return
            }
            val eleParent = PsiTreeUtil.findCommonParent(eleBgn, eleEnd)
            if (eleParent == null) {
                Messages.showWarningDialog("select filed not in same expression list", "Unsupported")
                return
            }

            val offBgn = eleBgn.startOffset
            val offEnd = eleEnd.endOffset
            val eleTgt = eleParent.children.filter {
                (it is PsiReferenceExpression || it is PsiMethodCallExpression)
                        && it.endOffset >= offBgn && it.startOffset <= offEnd
            }
            if (eleTgt.isEmpty()) {
                Messages.showWarningDialog("empty Jooq's field-like expression list", "Unsupported")
                return
            }
            for (ele in eleTgt) {
                if (ele is PsiReferenceExpression) {
                    val tp = ele.type!!
                    val psiClass = PsiTypesUtil.getPsiClass(tp)
                    val resolveClassInType = PsiUtil.resolveClassInType(tp)
                    val containingClass = psiClass?.containingClass
                    val ct = tp.canonicalText
                    val parentOfType = PsiTreeUtil.getParentOfType(ele, PsiClass::class.java)
                    val resolveClassInClassTypeOnly = PsiUtil.resolveClassInClassTypeOnly(tp)
                    println("")
                } else if (ele is PsiMethodCallExpression) {
                    val mod = ele.resolveMethod()!!
                    val tp = mod.returnType!!
                    val resolveClassInType = PsiUtil.resolveClassInType(tp)
                    val ct = tp.canonicalText
                    val psiClass = PsiTypesUtil.getPsiClass(tp)
                    val superClass = psiClass?.superClass
                    val containingClass = mod.containingClass
                    val resolveClassInClassTypeOnly = PsiUtil.resolveClassInClassTypeOnly(tp)
                    println("")
                }
            }
        } catch (t: Throwable) {
            IdeaUiHelper.showError("failed to generate by jooq", t)
        }
    }

    override fun update(e: AnActionEvent) {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabled = psiFile is PsiJavaFile
    }
}