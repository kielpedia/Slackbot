package com.loysen.slack.slackbot.web


import com.loysen.slack.slackbot.event.EventCallbackService
import com.loysen.slack.slackbot.event.EventResponse
import com.loysen.slack.slackbot.event.SlackMessage
import com.loysen.slack.slackbot.event.UrlVerificationService
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [EventHandlerController::class])
@RunWith(SpringRunner::class)
internal class EventHandlerControllerTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var urlVerificationService: UrlVerificationService
    @Autowired
    private lateinit var eventCallbackService: EventCallbackService

    @Test
    fun `Should route the url_verification message to UrlVerificationService`() {
        val message = SlackMessage(token = "token", type = "url_verification", challenge = "challenge", event = null)
        every { urlVerificationService.verifyToken(message) } returns EventResponse("challenge")
        val request = """
            {
            "token": "token",
            "type": "url_verification",
            "challenge":"challenge"
            }
        """.trimIndent()
        mockMvc.perform(post("")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.challenge").value("challenge"))
    }

    @Test
    fun `Should route the event_callback message to EventCallbackService`() {
        val request = """
            {
            "token": "token",
            "type": "event_callback",
            "challenge":"challenge"
            }
        """.trimIndent()
        mockMvc.perform(post("")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk)
                .andExpect(content().string("{}"))

        verify { eventCallbackService.handleCallback(any()) }
    }

    @Test
    fun `Ingore event_callback message that was retried`() {
        val request = """
            {
            "token": "token",
            "type": "event_callback",
            "challenge":"challenge"
            }
        """.trimIndent()
        mockMvc.perform(post("")
                .content(request)
                .header("X-Slack-Retry-Num", 1)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk)
                .andExpect(content().string("{}"))

        verify { eventCallbackService wasNot called }
    }

    @Test
    fun `Should return 200 for unknown type`() {
        val request = """
            {
            "token": "token",
            "type": "unknown"
            }
        """.trimIndent()
        mockMvc.perform(post("")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk)
                .andExpect(content().string("{}"))
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun urlVerificationService() = mockk<UrlVerificationService>()

        @Bean
        fun eventCallbackService() = mockk<EventCallbackService>(relaxed = true)
    }
}