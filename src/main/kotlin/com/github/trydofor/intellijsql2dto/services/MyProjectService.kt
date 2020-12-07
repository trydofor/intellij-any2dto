package com.github.trydofor.intellijsql2dto.services

import com.intellij.openapi.project.Project
import com.github.trydofor.intellijsql2dto.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
