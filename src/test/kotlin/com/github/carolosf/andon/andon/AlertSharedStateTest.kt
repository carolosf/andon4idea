package com.github.carolosf.andon.andon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class AlertSharedStateTest {
    private fun mockHttpGetGateway(response: String) = IHttpGetGateway {
        return@IHttpGetGateway response
    }

    @Test
    fun ensureAlertGroupsCreated() {
        // Arrange
        val content = """
                [{"name":"main", "url":"http://localhost:8080", "contributeToOverallStatus": true}]
            """.trimIndent()

        // Act
        AndonSharedState.updateAlertGroups(mockHttpGetGateway(content))

        // Assert
        val alertGroup = AndonSharedState.alertGroups["main"]!!
        assertEquals("main", alertGroup.name)
        assertEquals("http://localhost:8080", alertGroup.url)
        assertEquals(true, alertGroup.contributeToOverallStatus)
    }

    @Test
    fun ensureAlertGroupsStatusCreated() {
        // Arrange
        val user = "carolosfoscolos"
        val team = "myteam"
        val genericBlameText = "genericBlameText"
        val genericBlameTitle = "genericBlameTitle"
        val genericBlameIcon = "warn"

        val userBlameText = "userBlameText"
        val userBlameTitle = "userBlameTitle"
        val userBlameIcon = "error"

        val teamBlameText = "teamBlameText"
        val teamBlameTitle = "teamBlameTitle"
        val teamBlameIcon = "info"

        val content = """
        {"isHealthy":false, "blamedUsers":["$user"], "blamedTeams":["$team"],"genericBlameText":"$genericBlameText","genericBlameTitle":"$genericBlameTitle", "genericBlameIcon":"$genericBlameIcon", "userBlameText":"$userBlameText","userBlameTitle":"$userBlameTitle", "userBlameIcon":"$userBlameIcon", "teamBlameText":"$teamBlameText","teamBlameTitle":"$teamBlameTitle", "teamBlameIcon":"$teamBlameIcon"}
        """.trimIndent()

        //Act
        val alertGroup = AlertGroup("main", "http://localhost:8080")
        AndonSharedState.updateAlertGroupStatus(alertGroup, mockHttpGetGateway(content))

        //Assert
        assertFalse(alertGroup.status.isHealthy)
        assertTrue(alertGroup.status.isUserBlamed(user))
        assertTrue(alertGroup.status.isTeamBlamed(team))
        assertEquals(genericBlameText, alertGroup.status.genericBlameText)
        assertEquals(genericBlameTitle, alertGroup.status.genericBlameTitle)
        assertEquals(genericBlameIcon, alertGroup.status.genericBlameIcon)
        assertEquals(userBlameText, alertGroup.status.userBlameText)
        assertEquals(userBlameTitle, alertGroup.status.userBlameTitle)
        assertEquals(userBlameIcon, alertGroup.status.userBlameIcon)
        assertEquals(teamBlameText, alertGroup.status.teamBlameText)
        assertEquals(teamBlameTitle, alertGroup.status.teamBlameTitle)
        assertEquals(teamBlameIcon, alertGroup.status.teamBlameIcon)
    }

    @Test
    fun ensureAllHealthyWorksWhenAllHealthy() {
        // Arrange
        setUpAlertGroups()

        // Act
        AndonSharedState.alertGroups["main"]?.status = AlertGroupStatus(isHealthy = true)
        AndonSharedState.alertGroups["slow"]?.status = AlertGroupStatus(isHealthy = true)

        // Assert
        assertTrue(AndonSharedState.allHealthy())

    }

    @Test
    fun ensureNotAllHealthyWorksWhenNoneHealthy() {
        // Arrange
        setUpAlertGroups()

        // Act
        AndonSharedState.alertGroups["main"]?.status = AlertGroupStatus(isHealthy = false)
        AndonSharedState.alertGroups["slow"]?.status = AlertGroupStatus(isHealthy = false)

        // Assert
        assertFalse(AndonSharedState.allHealthy())
    }

    @Test
    fun ensureWhenNotContributingAllHealthy() {
        // Arrange
        setUpAlertGroups(false, false)

        // Act
        AndonSharedState.alertGroups["main"]?.status = AlertGroupStatus(isHealthy = false)
        AndonSharedState.alertGroups["slow"]?.status = AlertGroupStatus(isHealthy = false)

        // Assert
        assertTrue(AndonSharedState.allHealthy())

        // Arrange
        setUpAlertGroups(true, false)

        // Act
        AndonSharedState.alertGroups["main"]?.status = AlertGroupStatus(isHealthy = true)
        AndonSharedState.alertGroups["slow"]?.status = AlertGroupStatus(isHealthy = false)

        // Assert
        assertTrue(AndonSharedState.allHealthy())

        // Arrange
        setUpAlertGroups(false, true)

        // Act
        AndonSharedState.alertGroups["main"]?.status = AlertGroupStatus(isHealthy = false)
        AndonSharedState.alertGroups["slow"]?.status = AlertGroupStatus(isHealthy = true)

        // Assert
        assertTrue(AndonSharedState.allHealthy())

        // Genuine false cases

        // Arrange
        setUpAlertGroups(true, false)

        // Act
        AndonSharedState.alertGroups["main"]?.status = AlertGroupStatus(isHealthy = false)
        AndonSharedState.alertGroups["slow"]?.status = AlertGroupStatus(isHealthy = true)

        // Assert
        assertFalse(AndonSharedState.allHealthy())

        // Arrange
        setUpAlertGroups(false, true)

        // Act
        AndonSharedState.alertGroups["main"]?.status = AlertGroupStatus(isHealthy = true)
        AndonSharedState.alertGroups["slow"]?.status = AlertGroupStatus(isHealthy = false)

        // Assert
        assertFalse(AndonSharedState.allHealthy())
    }

    @Test
    fun ensureNotAllHealthyWorksWhenOneHealthy() {
        // Arrange
        setUpAlertGroups()

        // Act
        AndonSharedState.alertGroups["main"]?.status = AlertGroupStatus(isHealthy = true)
        AndonSharedState.alertGroups["slow"]?.status = AlertGroupStatus(isHealthy = false)

        // Assert
        assertFalse(AndonSharedState.allHealthy())

        //Act
        AndonSharedState.alertGroups["main"]?.status = AlertGroupStatus(isHealthy = false)
        AndonSharedState.alertGroups["slow"]?.status = AlertGroupStatus(isHealthy = true)

        // Assert
        assertFalse(AndonSharedState.allHealthy())
    }


    private fun setUpAlertGroups(mainContributes : Boolean = true, slowContributes : Boolean = true) {
        val alertGroupMain = AlertGroup("main", "http://localhost:8080", contributeToOverallStatus = mainContributes)
        val alertGroupSlow = AlertGroup("slow", "http://localhost:8080", contributeToOverallStatus = slowContributes)
        AndonSharedState.alertGroups = mapOf("main" to alertGroupMain, "slow" to alertGroupSlow)
    }
}