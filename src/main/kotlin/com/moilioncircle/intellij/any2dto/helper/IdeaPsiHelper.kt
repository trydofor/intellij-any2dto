package com.moilioncircle.intellij.any2dto.helper

import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiType

/**
 * @author trydofor
 * @since 2020-12-23
 */
object IdeaPsiHelper {

    fun inferMethodNaming(ele: PsiMethodCallExpression): String {
        val med = ele.methodExpression.text
        return if (med == "as" || med.endsWith(".as")) {
            ele.argumentList.text
        } else {
            ele.text
        }.substringAfter(".")
    }

    fun inferGenericType(psiType: PsiType, superClass: String, pos: Int): String {
        val def = psiType.canonicalText
        if (isSameClass(def, superClass)) {
            val p1 = def.indexOf('<')
            val p2 = def.lastIndexOf('>')
            return if (p1 in 1 until p2) {
                val pt = def.subSequence(p1 + 1, p2).split(',')
                pt[pos - 1]
            } else {
                def
            }
        } else {
            for (superType in psiType.superTypes) {
                val gt = inferGenericType(superType, superClass, pos)
                if (gt.isNotEmpty()) return gt
            }
        }
        return ""
    }

    private fun isSameClass(def: String, clz: String): Boolean {
        val len1 = def.length
        val len2 = clz.length
        return when {
            len1 < len2 -> false
            len1 == len2 -> def == clz
            else -> def.startsWith(clz) && def[len2] == '<'
        }
    }
}
