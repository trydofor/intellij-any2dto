package com.moilioncircle.intellij.any2dto.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.util.PsiTreeUtil
import com.moilioncircle.intellij.any2dto.helper.IdeaPsiHelper
import com.moilioncircle.intellij.any2dto.helper.IdeaUiHelper
import com.moilioncircle.intellij.any2dto.helper.MergerHelper
import com.moilioncircle.intellij.any2dto.helper.MergerHelper.FieldInfo
import com.moilioncircle.intellij.any2dto.helper.NamingFuns
import com.moilioncircle.intellij.any2dto.settings.SettingsState


class Any2DtoActionJooq : AnAction() {

    private val logger = Logger.getInstance(Any2DtoActionJooq::class.java)

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = IdeaUiHelper.jooqAccept(e)
    }

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
                Messages.showWarningDialog("fields not in same expression list", "Unsupported")
                return
            }

            val offBgn = eleBgn.textRange.startOffset
            val offEnd = eleEnd.textRange.endOffset
            val eleTgt = eleParent.children.filter {
                (it is PsiReferenceExpression || it is PsiMethodCallExpression)
                        && it.textRange.endOffset >= offBgn
                        && it.textRange.startOffset <= offEnd
            }
            if (eleTgt.isEmpty()) {
                Messages.showWarningDialog("empty Jooq's field-like expression list", "Unsupported")
                return
            }

            val project = e.getData(LangDataKeys.PROJECT)
            val fields = ArrayList<FieldInfo>()
            for (ele in eleTgt) {
                when (ele) {
                    is PsiReferenceExpression -> fields.add(byRefers(ele))
                    is PsiMethodCallExpression -> fields.add(byMethod(ele))
                    else -> logger.warn("unsupported type" + ele.text)
                }
            }

            val state = SettingsState.loadSettingState()
            MergerHelper.generateJava(state, fields, project, "Jooq Fields")
        } catch (t: Throwable) {
            logger.error("failed to generate by jooq", t)
            IdeaUiHelper.showError("failed to generate by jooq", t)
        }
    }

    // //// SelectField
    private fun byRefers(ele: PsiReferenceExpression): FieldInfo {
        val type = IdeaPsiHelper.inferGenericType(ele.type!!, "org.jooq.SelectField", 1)
        val name = NamingFuns.camelCase(ele.canonicalText.substringAfter('.'))
        return FieldInfo(name, type)
    }

    private fun byMethod(ele: PsiMethodCallExpression): FieldInfo {
        val type = IdeaPsiHelper.inferGenericType(ele.type!!, "org.jooq.SelectField", 1)
        val name = NamingFuns.camelCase(IdeaPsiHelper.inferMethodNaming(ele))
        return FieldInfo(name, type)
    }
}