package com.github.carolosf.andon.andon

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.WindowManager
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.swing.Icon

class ReminderProjectActivity : ProjectActivity, DumbAware {
    companion object {
        private val LOG = Logger.getInstance(ReminderProjectActivity::class.java)
        public fun getIcon(icon: String): Icon {
            return when (icon) {
                "error" -> Messages.getErrorIcon()
                "warn" -> Messages.getWarningIcon()
                "info" -> Messages.getInformationIcon()
                "question" -> Messages.getQuestionIcon()
                else -> Messages.getWarningIcon()
            }
        }
    }
    override suspend fun execute(project: Project) {
        //TODO make alternative config options
        val userName = System.getProperty("user.name")

        val teamNameEnvVar = System.getenv("ANDON_PLUGIN_TEAM_NAME")
        val teamName = if(!teamNameEnvVar.isNullOrBlank()) teamNameEnvVar else ""

        val alertGroupsURLEnvVar = System.getenv("ANDON_PLUGIN_DISCOVERY_URL")
        val alertGroupsURL = if(!alertGroupsURLEnvVar.isNullOrBlank()) alertGroupsURLEnvVar else ""

        val pollTimeEnvVar = System.getenv("ANDON_PLUGIN_POLL_TIME_SECONDS")
        val pollTime = if(!pollTimeEnvVar.isNullOrBlank()) pollTimeEnvVar.toLong() else 300

        AndonSharedState.updateAlertGroups(httpGetGateway(alertGroupsURL))

        LOG.debug("Init finished - setting up scheduled executor")
        val executor = Executors.newSingleThreadScheduledExecutor()
        executor.scheduleAtFixedRate({
            // these executions seem to pile up if the message box is called - kind of resource leak if you leave the ide open
            ApplicationManager.getApplication().invokeLater {
                val disposed = project.isDisposed
                AndonSharedState.alertGroups.forEach { (_, alertGroup) ->
                    AndonSharedState.updateAlertGroupStatus(
                        alertGroup,
                        httpGetGateway(alertGroup.url)
                    )
                    processAlertGroup(disposed, alertGroup, project, userName, teamName)
                }
                updateStatusBarUI()
            }
        }, 0, pollTime, TimeUnit.SECONDS)
    }

    private fun httpGetGateway(url: String) = IHttpGetGateway {
        val url = URL(url)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.setRequestMethod("GET")
        connection.setRequestProperty("User-Agent", "AndonIntelliJPlugin")
        // TODO maybe handle ! connection.responseCode in 200 .. 299

        val content = connection.inputStream.bufferedReader().use { it.readText() }
        return@IHttpGetGateway content
    }

    private fun processAlertGroup(
        disposed: Boolean,
        alertGroup: AlertGroup,
        project: Project,
        userName: String,
        teamName: String
    ) {
        if (
            !disposed &&
            alertGroup.lastAlertTime.isBefore(LocalDateTime.now().minus(Duration.ofSeconds(alertGroup.status.rateLimitInSeconds))) &&
            !alertGroup.isAlerting.getAndSet(true)
        ) {
            if (!alertGroup.status.isHealthy) {
                if (alertGroup.status.isUserBlamed(userName)) {
                    if (alertGroup.status.userBlameText.isNotBlank()) {
                        Messages.showMessageDialog(
                            project,
                            alertGroup.status.userBlameText,
                            alertGroup.status.userBlameTitle,
                            Messages.getErrorIcon()
                        )
                    }
                } else if (alertGroup.status.isTeamBlamed(teamName)) {
                    if (alertGroup.status.teamBlameText.isNotBlank()) {
                        Messages.showMessageDialog(
                            project,
                            alertGroup.status.teamBlameText,
                            alertGroup.status.teamBlameTitle,
                            Messages.getErrorIcon()
                        )
                    }
                } else {
                    if (alertGroup.status.genericBlameText.isNotBlank()) {
                        Messages.showMessageDialog(
                            project,
                            alertGroup.status.genericBlameText,
                            alertGroup.status.genericBlameTitle,
                            getIcon(alertGroup.status.genericBlameIcon)
                        )
                    }
                }
                alertGroup.lastAlertTime = LocalDateTime.now()
                alertGroup.isAlerting.set(false)
            }
        }
    }



    private fun updateStatusBarUI() {
        ProjectManager.getInstance().openProjects.forEach { project ->
            val statusBar = WindowManager.getInstance().getStatusBar(project)
            statusBar?.updateWidget(StatusIconWidget.Factory.ID)
        }
    }
}
