package com.moilioncircle.intellij.any2dto.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
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
    var javaDtoPrompt: Boolean = true,
    var usingClipboard: Boolean = true,
    var usingInnerClass: Boolean = true,
    var javaTempletInner: String = ConfigHelper.defaultTemplateInner,
    var javaTempletOuter: String = ConfigHelper.defaultTemplateOuter,
    var codeTempletReview: String = ConfigHelper.defaultTemplateReview,
    var javaTypeMapping: String = ConfigHelper.defaultMapping,
    var textLineSeparator: String = defaultLineSep,
    var textLinePrompt: Boolean = true,
    var textWordSeparator: String = defaultWordSep,
    var textSqlTable: String = defaultSqlTable,
    var textSqlColumn: String = defaultSqlColumn,
    var textSqlDsl: String = defaultSqlDsl,
    var textDslName: String = defaultDslName,
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
        javaDtoPrompt = true
        usingClipboard = true
        usingInnerClass = true
        javaTempletInner = ConfigHelper.defaultTemplateInner
        javaTempletOuter = ConfigHelper.defaultTemplateOuter
        codeTempletReview = ConfigHelper.defaultTemplateReview
        javaTypeMapping = ConfigHelper.defaultMapping
        textLineSeparator = defaultLineSep
        textLinePrompt = true
        textWordSeparator = defaultWordSep
        textSqlTable = defaultSqlTable
        textSqlColumn = defaultSqlColumn
        textSqlDsl = defaultSqlDsl
        textDslName = defaultDslName
    }

    companion object {
        const val defaultDtoName = "Dto"
        const val defaultSrcPath = "./src/main/java"
        const val defaultPkgName = "com.moilioncircle.autogen"
        const val defaultLineSep = """[\r\n,;]+"""
        const val defaultWordSep = """[^a-z0-9A-Z]+"""
        const val defaultSqlTable = """{tab|PascalCase}Table {ref} = {tab|camelCase}Dao.getTable()"""
        const val defaultSqlColumn = """{col|PascalCase}"""
        const val defaultSqlDsl = "DSLContext ctx = {tab|camelCase}Dao.ctx()"
        const val defaultDslName = "ctx"
        fun loadSettingState(project: Project): SettingsState = project.getService(SettingsState::class.java)
    }
}
