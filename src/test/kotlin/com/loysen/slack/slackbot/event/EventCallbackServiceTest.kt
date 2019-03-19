package com.loysen.slack.slackbot.event

import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class EventCallbackServiceTest {

    private val properties = SlackProperties()
    private val service: EventCallbackService = EventCallbackService(properties)

    @Test
    fun `Should read and make external call for message containing the key`() {
        val input = SlackEvent(type = "message", channel = "channel", text = "Kotlin", user = "user")
        val expected = EventResponse(null)

        val result = service.handleCallback(input)

        assertThat(result, `is`(expected))
    }

    @Test
    fun `Should read and do nothing for message that doesnt match our key`() {
        val input = SlackEvent(type = "message", channel = "channel", text = "random", user = "user")
        val expected = EventResponse(null)

        val result = service.handleCallback(input)

        assertThat(result, `is`(expected))
    }

    @Test
    fun `Should read and do nothing for unknown type`() {
        val input = SlackEvent(type = "unknow", channel = "channel", text = "random", user = "user")
        val expected = EventResponse(null)

        val result = service.handleCallback(input)

        assertThat(result, `is`(expected))
    }

    @Test
    fun `Should read and do nothing for null event`() {
        val expected = EventResponse(null)

        val result = service.handleCallback(null)

        assertThat(result, `is`(expected))
    }
}