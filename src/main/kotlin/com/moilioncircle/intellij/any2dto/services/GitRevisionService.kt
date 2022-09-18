package com.moilioncircle.intellij.any2dto.services

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
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
            ProgressManager.getInstance().run(
                object : Task.WithResult<String?, Exception>(project, "Loading file git revision", false) {
                    override fun compute(indicator: ProgressIndicator): String? {
                        val repo = GitUtil.getRepositoryForFile(project, file)
                        return repo.currentRevision
                    }
                }
            )
        } catch (e: Exception) {
            return null;
        }
    }
}
