package com.loysen.slack.slackbot.event

import org.junit.Assert.assertThat
import org.junit.Test

import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNull.nullValue
import org.junit.Before

class UrlVerificationServiceTest {

    private val properties = SlackProperties()
    private val service: UrlVerificationService = UrlVerificationService(properties)

    @Before
    fun setup() {
        properties.verificationToken = "testToken"
    }

    @Test
    fun `Should return challenge for correct token`() {
        val input = EventMessage(token = "testToken", type = "url_verification", challenge = "challenge", event = null)

        val result = service.verifyToken(input)

        assertThat(result.challenge, `is`("challenge"))
    }

    @Test
    fun `Should not return challenge for incorrect token`() {
        val input = EventMessage(token = "wrong token", type = "url_verification", challenge = "challenge", event = null)

        val result = service.verifyToken(input)

        assertThat(result.challenge, nullValue())
    }


}