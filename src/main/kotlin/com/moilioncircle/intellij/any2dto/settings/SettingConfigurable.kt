package com.moilioncircle.intellij.any2dto.settings

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.options.SearchableConfigurable
import com.moilioncircle.intellij.any2dto.ui.SettingComponent
import javax.swing.JComponent
import javax.swing.event.ChangeEvent

/**
 * @author trydofor
 * @since 2020-12-17
 */
class SettingConfigurable : SearchableConfigurable {

    var component: SettingComponent? = null

    override fun createComponent(): JComponent {
        component = SettingComponent()
        val state = ServiceManager.getService(SettingsState::class.java)
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
                || state.javaTempletInner != txtTmplInner.text
                || state.javaTempletOuter != txtTmplOuter.text
                || state.javaTypeMapping != txtTypeMapping.text
    }

    override fun apply() = with(component!!) {
        val state = SettingsState.loadSettingState()
        state.usingClipboard = rbtClipboard.isSelected
        state.usingInnerClass = rbtInnerClass.isSelected
        state.javaPackageName = txtPackageName.text
        state.javaSourcePath = txtSourcePath.text
        state.javaDtoName = txtDtoName.text
        state.javaTempletInner = txtTmplInner.text
        state.javaTempletOuter = txtTmplOuter.text
        state.javaTypeMapping = txtTypeMapping.text
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

        txtSourcePath.toolTipText = "[./] means current project"
    }

    private fun initStateValue(state: SettingsState) = with(component!!) {
        if (state.usingClipboard) {
            rbtClipboard.isSelected = true
            rbtSourcePath.isSelected = false
        } else {
            rbtClipboard.isSelected = false
            rbtSourcePath.isSelected = true
        }
        if(state.usingInnerClass) {
            rbtInnerClass.isSelected = true
            rbtOuterFile.isSelected = false
        }else{
            rbtInnerClass.isSelected = false
            rbtOuterFile.isSelected = true
        }

        txtSourcePath.text = state.javaSourcePath
        txtPackageName.text = state.javaPackageName
        txtDtoName.text = state.javaDtoName
        txtTypeMapping.text = state.javaTypeMapping
        txtTmplInner.text = state.javaTempletInner
        txtTmplOuter.text = state.javaTempletOuter
        whereSaveState(null)
    }
}