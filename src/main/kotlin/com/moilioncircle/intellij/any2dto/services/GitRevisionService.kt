package com.moilioncircle.intellij.any2dto.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitUtil

/**
 * @author trydofor
 * @since 2022-09-14
 */
class GitRevisionService {

    fun getRevision(project: Project, file: VirtualFile): String? {
        return try {
            val repo = GitUtil.getRepositoryForFile(project, file)
            repo.currentRevision
        } catch (e: Exception) {
            null
        }
    }
}
