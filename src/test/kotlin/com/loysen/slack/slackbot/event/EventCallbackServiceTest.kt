package com.loysen.slack.slackbot.event

import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.lang.RuntimeException
import java.net.URI

class EventCallbackServiceTest {

    private val properties = SlackProperties()
    private val restTemplate = mockk<RestTemplate>()
    private val service: EventCallbackService = EventCallbackService(properties, restTemplate)

    @Test
    fun `Should read and make successful external call for message containing the key`() {
        every {
            restTemplate.postForEntity(
                    URI(properties.slackMessagePostUrl),
                    CreateMessage(properties.messageToken, "channel", "Kotlin is fun"),
                    Void::class.java)
        } returns ResponseEntity.ok().build<Void>()
        val input = SlackEvent(type = "message", channel = "channel", text = "kotlin", user = "user")

        service.handleCallback(input)
    }

    @Test
    fun `Should read and make failing external call for message containing the key`() {
        every {
            restTemplate.postForEntity(
                URI(properties.slackMessagePostUrl),
                CreateMessage(properties.messageToken, "channel", "Kotlin is fun"),
                Void::class.java)
        } returns ResponseEntity.status(500).build()
        val input = SlackEvent(type = "message", channel = "channel", text = "kotlin", user = "user")

        service.handleCallback(input)
    }

    @Test
    fun `Should read and make a call that throws an exception without leaking`() {
        every {
            restTemplate.postForEntity(
                    URI(properties.slackMessagePostUrl),
                    CreateMessage(properties.messageToken, "channel", "Kotlin is fun"),
                    Void::class.java)
        } throws RuntimeException()
        val input = SlackEvent(type = "message", channel = "channel", text = "kotlin", user = "user")

        service.handleCallback(input)
    }

    @Test
    fun `Should read and do nothing for message that doesnt match our key`() {
        val input = SlackEvent(type = "message", channel = "channel", text = "random", user = "user")

        service.handleCallback(input)

        verify{ restTemplate wasNot Called }
    }

    @Test
    fun `Should ignore bot message`() {
        val input = SlackEvent(type = "message", channel = "channel", text = "kotlin", user = "user", subtype = "BOT")

        service.handleCallback(input)

        verify{ restTemplate wasNot Called }
    }

    @Test
    fun `Should read and do nothing for unknown type`() {
        val input = SlackEvent(type = "unknow", channel = "channel", text = "random", user = "user")

        service.handleCallback(input)

        verify{ restTemplate wasNot Called }
    }

    @Test
    fun `Should read and do nothing for null event`() {
        service.handleCallback(null)

        verify{ restTemplate wasNot Called }
    }
}