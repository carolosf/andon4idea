package com.github.carolosf.andon.andon

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.IconLoader
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.LocalDateTime

fun interface IHttpGetGateway {
    fun getContent(url : String): String
}
class AndonSharedState {
    companion object {
        private val LOG = Logger.getInstance(AndonSharedState::class.java)

        const val ICON_HEIGHT = 16
        val ICON_BROKEN = IconLoader.getIcon("/icons/fire.svg", AndonSharedState::class.java)
        val ICON_OK = IconLoader.getIcon("/icons/green-circle.svg", AndonSharedState::class.java)

        var alertGroups : Map<String, AlertGroup> = mapOf()

        fun allHealthy(): Boolean {
            return alertGroups.values.fold(true) { acc, it ->
                if (it.contributeToOverallStatus) acc && it.status.isHealthy else acc
            }
        }

        fun updateAlertGroups(httpGateway: IHttpGetGateway) {
            val content = httpGateway.getContent("http://localhost:8080/alertgroups.html")
            val alertGroupsList : List<AlertGroup> = Json.decodeFromString(content)
            alertGroups = alertGroupsList.associateBy { it.name }
            LOG.debug(alertGroups.toString())
        }
        fun updateAlertGroupStatus(alertGroup: AlertGroup, httpGateway: IHttpGetGateway) {
            if (alertGroup.lastStatusUpdate.isAfter(LocalDateTime.now().minus(Duration.ofSeconds(alertGroup.status.rateLimitInSeconds)))) {
                return
            }

            try {
                val content = httpGateway.getContent(alertGroup.url)
                alertGroup.status = Json.decodeFromString(content)
                LOG.debug(Json.encodeToString(alertGroup.status))
            } catch ( e : Exception) {
                LOG.error(e)
            }
        }
    }
}