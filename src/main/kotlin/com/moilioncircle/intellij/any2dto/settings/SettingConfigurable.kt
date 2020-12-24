package com.moilioncircle.intellij.any2dto.settings

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.moilioncircle.intellij.any2dto.ui.SettingComponent
import javax.swing.JComponent
import javax.swing.event.ChangeEvent


/**
 * @author trydofor
 * @since 2020-12-17
 */
class SettingConfigurable(val project: Project) : SearchableConfigurable {

    var component: SettingComponent? = null

    override fun createComponent(): JComponent {
        val state = ServiceManager.getService(SettingsState::class.java)
        component = SettingComponent(project, state.javaTypeMapping, state.javaTempletInner, state.javaTempletOuter)
        initComponentEvent(state)
        initStateValue(state)
        return component!!.pnlRoot
    }

    override fun isModified(): Boolean = with(component!!) {
        val state = SettingsState.loadSettingState()
        return state.usingClipboard != rbtClipboard.isSelected
                || state.usingInnerClass != rbtInnerClass.isSelected
                || state.javaPackageName != txtPackageName.text
                || state.javaSourcePath != txtSourcePath.text
                || state.javaDtoName != txtDtoName.text
                || state.javaDtoPromote != ckbDtoPromote.isSelected
                || state.javaTempletInner != edtTmplInner.text
                || state.javaTempletOuter != edtTmplOuter.text
                || state.javaTypeMapping != edtTypeMapping.text
                || state.textLineSeparator != txtTextLineSep.text
                || state.textLinePromote != ckbLinePromote.isSelected
                || state.textWordSeparator != txtTextWordSep.text
    }

    override fun apply() = with(component!!) {
        val state = SettingsState.loadSettingState()
        state.usingClipboard = rbtClipboard.isSelected
        state.usingInnerClass = rbtInnerClass.isSelected
        state.javaPackageName = txtPackageName.text
        state.javaSourcePath = txtSourcePath.text
        state.javaDtoPromote = ckbDtoPromote.isSelected
        state.javaDtoName = txtDtoName.text
        state.javaTempletInner = edtTmplInner.text
        state.javaTempletOuter = edtTmplOuter.text
        state.javaTypeMapping = edtTypeMapping.text
        state.textLineSeparator = txtTextLineSep.text
        state.textLinePromote = ckbLinePromote.isSelected
        state.textWordSeparator = txtTextWordSep.text
    }

    override fun reset() = with(component!!) {
        val state = SettingsState.loadSettingState()
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
        ckbDtoPromote.isSelected = state.javaDtoPromote
        edtTypeMapping.text = state.javaTypeMapping
//        edtTmplInner.text = state.javaTempletInner
        edtTmplOuter.text = state.javaTempletOuter
        txtTextLineSep.text = state.textLineSeparator
        ckbLinePromote.isSelected = state.textLinePromote
        txtTextWordSep.text = state.textWordSeparator
        whereSaveState(null)
    }
}