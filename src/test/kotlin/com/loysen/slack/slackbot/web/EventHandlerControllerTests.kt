package com.loysen.slack.slackbot.web


import com.loysen.slack.slackbot.event.EventCallbackService
import com.loysen.slack.slackbot.event.EventResponse
import com.loysen.slack.slackbot.event.EventMessage
import com.loysen.slack.slackbot.event.UrlVerificationService
import com.loysen.slack.slackbot.verification.RequestValidator
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
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@WebMvcTest(controllers = [EventHandlerController::class])
@RunWith(SpringRunner::class)
internal class EventHandlerControllerTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var urlVerificationService: UrlVerificationService
    @Autowired
    private lateinit var eventCallbackService: EventCallbackService
    @Autowired
    private lateinit var requestValidator: RequestValidator

    @Test
    fun `Should route the url_verification message to UrlVerificationService`() {
        val message = EventMessage(token = "token", type = "url_verification", challenge = "challenge", event = null)
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
                .header(SlackHeaders.NUM_RETRIES, 1)
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

    @Test
    fun `Should handle a command post`() {
        val requestTime = 100L
        val signature = "SIGNATURE"
        every { requestValidator.verifyRequest(signature, any(), requestTime, any()) } returns true
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        with(params) {
            add("text", "text")
            add("command", "command")
            add("response_url", "www.google.com")
        }
        mockMvc.perform(post("")
                .header(SlackHeaders.REQUEST_TIME, requestTime)
                .header(SlackHeaders.SIGNATURE, signature)
                .params(params)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .andExpect(status().isOk)
    }

    @Test
    fun `Should reject a post that fails verification`() {
        val requestTime = 100L
        val signature = "SIGNATURE"
        every { requestValidator.verifyRequest(any(), any(), any(), any()) } returns false
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        with(params) {
            add("text", "text")
            add("command", "command")
            add("response_url", "www.google.com")
        }
        mockMvc.perform(post("")
                .header(SlackHeaders.REQUEST_TIME, requestTime)
                .header(SlackHeaders.SIGNATURE, signature)
                .params(params)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .andExpect(status().`is`(403))
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun urlVerificationService() = mockk<UrlVerificationService>()

        @Bean
        fun eventCallbackService() = mockk<EventCallbackService>(relaxed = true)

        @Bean
        fun requestValidator() = mockk<RequestValidator>()
    }
}