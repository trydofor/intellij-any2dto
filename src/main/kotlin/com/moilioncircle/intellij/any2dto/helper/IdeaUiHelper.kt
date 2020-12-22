package com.moilioncircle.intellij.any2dto.helper

import com.intellij.openapi.ui.Messages
import java.io.PrintWriter
import java.io.StringWriter

/**
 * @author trydofor
 * @since 2020-12-21
 */
object IdeaUiHelper {
    fun showError(msg: String, t: Throwable) {
        val out = StringWriter()
        val p = PrintWriter(out)
        t.printStackTrace(p)
        Messages.showMultilineInputDialog(null,
            msg,
            "Notice",
            out.toString(),
            Messages.getErrorIcon(),
            null)
    }

}