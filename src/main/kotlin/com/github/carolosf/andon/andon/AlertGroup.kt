package com.github.carolosf.andon.andon

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Serializable
data class AlertGroupStatus(
    val isHealthy: Boolean = true,
    val blamedUsers: List<String> = listOf(),
    val blamedTeams: List<String> = listOf(),

    val genericBlameText: String = "",
    val genericBlameTitle: String = "",
    val genericBlameIcon: String = "warn",

    val userBlameText: String = "",
    val userBlameTitle: String = "",
    val userBlameIcon: String = "warn",

    val teamBlameText: String = "",
    val teamBlameTitle: String = "",
    val teamBlameIcon: String = "warn",

    // config for how often users should be alerted about this alert group
    val rateLimitInSeconds : Long = 300,
) {
    fun isUserBlamed(userName: String): Boolean {
        return blamedUsers.contains(userName)
    }

    fun isTeamBlamed(teamName: String): Boolean {
        return blamedTeams.contains(teamName)
    }
}
@Serializable
data class AlertGroup (
    val name: String,
    val url: String,
    val contributeToOverallStatus: Boolean = false,

    @Transient
    var status: AlertGroupStatus = AlertGroupStatus(),

    // These are runtime variables we need for the plugin UI
    @Transient
    val isAlerting: AtomicBoolean = AtomicBoolean(false),

    @Transient
    var lastAlertTime: LocalDateTime = LocalDateTime.now().minusDays(1),

    @Transient
    var lastStatusUpdate: LocalDateTime = LocalDateTime.now().minusDays(1),
)