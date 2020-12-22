package com.moilioncircle.intellij.any2dto.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.moilioncircle.intellij.any2dto.helper.ConfigHelper

/**
 * @author trydofor
 * @since 2020-12-17
 */
@State(name = "MoilionCircle.Any2dto.Settings", storages = [Storage("moilioncircle-any2dto.xml")])
data class SettingsState(
    var javaSourcePath: String = defaultSrcPath,
    var javaPackageName: String = defaultPkgName,
    var javaDtoName: String = defaultDtoName,
    var usingClipboard: Boolean = true,
    var usingInnerClass: Boolean = true,
    var javaTempletInner: String = ConfigHelper.defaultTemplateInner,
    var javaTempletOuter: String = ConfigHelper.defaultTemplateOuter,
    var javaTypeMapping: String = ConfigHelper.defaultMapping,
) : PersistentStateComponent<SettingsState> {

    override fun getState(): SettingsState {
        return this
    }

    override fun loadState(state: SettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun loadDefaultState() {
        javaSourcePath = defaultSrcPath
        javaPackageName = defaultPkgName
        javaDtoName = defaultDtoName
        usingClipboard = true
        usingInnerClass = true
        javaTempletInner = ConfigHelper.defaultTemplateInner
        javaTempletOuter = ConfigHelper.defaultTemplateOuter
        javaTypeMapping = ConfigHelper.defaultMapping
    }

    companion object {
        const val defaultDtoName = "Dto"
        const val defaultSrcPath = "./src/main/java"
        const val defaultPkgName = "com.moilioncircle.autogen"
        fun loadSettingState(): SettingsState = ServiceManager.getService(SettingsState::class.java)
    }
}