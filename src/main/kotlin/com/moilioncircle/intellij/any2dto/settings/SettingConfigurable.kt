package com.moilioncircle.intellij.any2dto.settings

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.moilioncircle.intellij.any2dto.ui.SettingComponent
import javax.swing.JComponent
import javax.swing.event.ChangeEvent

/**
 * @author trydofor
 * @since 2020-12-17
 */
class SettingConfigurable(private val project: Project) : SearchableConfigurable {

    var component: SettingComponent? = null

    override fun createComponent(): JComponent {
        val state = SettingsState.loadSettingState(project)
        component = SettingComponent(
            project,
            state.javaTypeMapping,
            state.javaTempletInner,
            state.javaTempletOuter,
            state.codeTempletReview
        )
        initComponentEvent(state)
        initStateValue(state)
        return component!!.pnlRoot
    }

    override fun isModified(): Boolean = with(component!!) {
        val state = SettingsState.loadSettingState(project)
        return state.usingClipboard != rbtClipboard.isSelected
                || state.usingInnerClass != rbtInnerClass.isSelected
                || state.javaPackageName != txtPackageName.text
                || state.javaSourcePath != txtSourcePath.text
                || state.javaDtoName != txtDtoName.text
                || state.javaDtoPrompt != ckbDtoPrompt.isSelected
                || state.javaTempletInner != edtTmplInner.text
                || state.javaTempletOuter != edtTmplOuter.text
                || state.codeTempletReview != edtTmplReview.text
                || state.javaTypeMapping != edtTypeMapping.text
                || state.textLineSeparator != txtTextLineSep.text
                || state.textLinePrompt != ckbLinePrompt.isSelected
                || state.textWordSeparator != txtTextWordSep.text
                || state.textSqlTable != txtSqlTable.text
                || state.textSqlColumn != txtSqlColumn.text
                || state.textSqlDsl != txtSqlDsl.text
                || state.textDslName != txtDslName.text
    }

    override fun apply() = with(component!!) {
        val state = SettingsState.loadSettingState(project)
        state.usingClipboard = rbtClipboard.isSelected
        state.usingInnerClass = rbtInnerClass.isSelected
        state.javaPackageName = txtPackageName.text
        state.javaSourcePath = txtSourcePath.text
        state.javaDtoPrompt = ckbDtoPrompt.isSelected
        state.javaDtoName = txtDtoName.text
        state.javaTempletInner = edtTmplInner.text
        state.javaTempletOuter = edtTmplOuter.text
        state.codeTempletReview = edtTmplReview.text
        state.javaTypeMapping = edtTypeMapping.text
        state.textLineSeparator = txtTextLineSep.text
        state.textLinePrompt = ckbLinePrompt.isSelected
        state.textWordSeparator = txtTextWordSep.text
        state.textSqlTable = txtSqlTable.text
        state.textSqlColumn = txtSqlColumn.text
        state.textSqlDsl = txtSqlDsl.text
        state.textDslName = txtDslName.text
    }

    override fun reset() = with(component!!) {
        val state = SettingsState.loadSettingState(project)
        initStateValue(state)
    }

    override fun disposeUIResources() {
        component = null
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return component!!.rbtClipboard
    }

    override fun getDisplayName(): String {
        return "Any2dto"
    }

    override fun getId(): String {
        return "MoilionCircle.Any2dtoSettings"
    }

    private val whereSaveState: (e: ChangeEvent?) -> Unit = {
        with(component!!) {
            if (rbtClipboard.isSelected) {
                txtSourcePath.isEnabled = false
                txtPackageName.isEnabled = false
            } else {
                txtSourcePath.isEnabled = true
                txtPackageName.isEnabled = true
            }
        }
    }

    private fun initComponentEvent(state: SettingsState) = with(component!!) {
        btnLoadDefault.addActionListener {
            state.loadDefaultState()
            initStateValue(state)
        }

        rbtClipboard.addChangeListener(whereSaveState)
        rbtSourcePath.addChangeListener(whereSaveState)
        btnPluginHome.addActionListener { BrowserUtil.browse("https://github.com/trydofor/intellij-any2dto"); }
        btnMeepoHelp.addActionListener { BrowserUtil.browse("https://github.com/trydofor/pro.fessional.meepo"); }
    }

    private fun initStateValue(state: SettingsState) = with(component!!) {
        if (state.usingClipboard) {
            rbtClipboard.isSelected = true
            rbtSourcePath.isSelected = false
        } else {
            rbtClipboard.isSelected = false
            rbtSourcePath.isSelected = true
        }
        if (state.usingInnerClass) {
            rbtInnerClass.isSelected = true
            rbtOuterFile.isSelected = false
        } else {
            rbtInnerClass.isSelected = false
            rbtOuterFile.isSelected = true
        }

        txtSourcePath.text = state.javaSourcePath
        txtPackageName.text = state.javaPackageName
        txtDtoName.text = state.javaDtoName
        ckbDtoPrompt.isSelected = state.javaDtoPrompt
        edtTypeMapping.text = state.javaTypeMapping
        edtTmplInner.text = state.javaTempletInner
        edtTmplOuter.text = state.javaTempletOuter
        edtTmplReview.text = state.codeTempletReview
        txtTextLineSep.text = state.textLineSeparator
        ckbLinePrompt.isSelected = state.textLinePrompt
        txtTextWordSep.text = state.textWordSeparator
        txtSqlTable.text = state.textSqlTable
        txtSqlColumn.text = state.textSqlColumn
        txtSqlDsl.text = state.textSqlDsl
        txtDslName.text = state.textDslName

        whereSaveState(null)
    }
}
