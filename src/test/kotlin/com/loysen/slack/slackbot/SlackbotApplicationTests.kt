package com.loysen.slack.slackbot

import com.loysen.slack.slackbot.event.*
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate
import java.net.URI

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["test"])
class SlackbotApplicationTests {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate
    @Autowired
    lateinit var properties: SlackProperties
    @Autowired
    lateinit var slackRestTemplate: RestTemplate

    @Test
    fun `Returns successful verification`() {
        val message = SlackMessage(properties.verificationToken, "challenge", "url_verification")
        val response: ResponseEntity<EventResponse> = testRestTemplate.postForEntity(URI("/"), message, EventResponse::class.java)

        assertThat(response.statusCodeValue, `is`(200))
        assertThat(response.body?.challenge, `is`("challenge"))
    }

    @Test
    fun `Sends response message to Slack for a matching event`() {
        val message = SlackMessage(
                token = properties.verificationToken,
                type = "event_callback",
                event = SlackEvent(type = "message", channel = "channel", text = "kotlin"))
        val response: ResponseEntity<EventResponse> = testRestTemplate.postForEntity(URI("/"), message, EventResponse::class.java)

        assertThat(response.statusCodeValue, `is`(200))

        verify {
            slackRestTemplate.postForEntity(
                    URI(properties.slackMessagePostUrl),
                    CreateMessage(properties.messageToken, "channel", "Kotlin is fun"),
                    Void::class.java
            )
        }
    }

    @Test
    fun `Ignore message to Slack for a matching event from bot`() {
        val message = SlackMessage(
                token = properties.verificationToken,
                type = "event_callback",
                event = SlackEvent(type = "message", channel = "channel", text = "kotlin", subtype = "bot_message"))
        val response: ResponseEntity<EventResponse> = testRestTemplate.postForEntity(URI("/"), message, EventResponse::class.java)

        assertThat(response.statusCodeValue, `is`(200))

        verify{ slackRestTemplate wasNot Called }
    }

    @Test
    fun `Sends success response even if the Slack call fails`() {
        every {
            slackRestTemplate.postForEntity(
                    URI(properties.slackMessagePostUrl),
                    CreateMessage(properties.messageToken, "channe", "Kotlin is fun"),
                    Void::class.java)
        } throws RuntimeException()
        val message = SlackMessage(
                token = properties.verificationToken,
                type = "event_callback",
                event = SlackEvent(type = "message", channel = "channe", text = "kotlin"))
        val response: ResponseEntity<EventResponse> = testRestTemplate.postForEntity(URI("/"), message, EventResponse::class.java)

        assertThat(response.statusCodeValue, `is`(200))
    }

    @Test
    fun `Returns an empty 200 response for every other request`() {
        val message = SlackMessage(properties.verificationToken, type = "unknown")
        val response: ResponseEntity<EventResponse> = testRestTemplate.postForEntity(URI("/"), message, EventResponse::class.java)

        assertThat(response.statusCodeValue, `is`(200))
    }

    @TestConfiguration
    @Profile("test")
    class TestConfig {
        @Bean
        fun restTemplate() = mockk<RestTemplate>()
    }

}
