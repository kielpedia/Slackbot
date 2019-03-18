package com.loysen.slack.slackbot.web


import com.loysen.slack.slackbot.event.EventCallbackService
import com.loysen.slack.slackbot.event.EventResponse
import com.loysen.slack.slackbot.event.SlackMessage
import com.loysen.slack.slackbot.event.UrlVerificationService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
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

    @MockkBean
    private lateinit var urlVerificationService: UrlVerificationService
    @MockkBean
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
        every { eventCallbackService.handleCallback(null) } returns EventResponse(null)
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
}