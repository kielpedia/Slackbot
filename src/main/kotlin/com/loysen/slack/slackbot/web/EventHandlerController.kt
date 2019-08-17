package com.loysen.slack.slackbot.web

import com.loysen.slack.slackbot.event.*
import com.loysen.slack.slackbot.verification.RequestValidator
import com.loysen.slack.slackbot.web.SlackHeaders.Companion.NUM_RETRIES
import com.loysen.slack.slackbot.web.SlackHeaders.Companion.REQUEST_TIME
import com.loysen.slack.slackbot.web.SlackHeaders.Companion.SIGNATURE
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
class EventHandlerController @Autowired constructor(private val urlVerificationService: UrlVerificationService,
                                                    private val eventCallbackService: EventCallbackService,
                                                    private val requestValidtor: RequestValidator) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun handleSlackEvent(@Valid @RequestBody message: EventMessage,
                         @RequestHeader(name = NUM_RETRIES, required = false) requestCount: Int?): EventResponse {

        if (message.type == "url_verification") {
            return urlVerificationService.verifyToken(message)
        } else if (message.type == "event_callback" && requestCount == null) {
            eventCallbackService.handleCallback(message.event)
        }

        return EventResponse(null)
    }

    @PostMapping(consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun handleSlackCommand(@RequestParam text: String,
                           @RequestParam command: String,
                           @RequestParam(name = "response_url") responseUrl: String,
                           @RequestHeader(name = SIGNATURE) signature: String,
                           @RequestHeader(name = REQUEST_TIME) requestTimeMillis: Long,
                           @RequestHeader(name = NUM_RETRIES, required = false) requestCount: Int?,
                           @RequestBody rawBody: String): ResponseEntity<CommandResponse> {
        val nowMillis = Instant.now().toEpochMilli()
        if (!requestValidtor.verifyRequest(signature, rawBody, requestTimeMillis, nowMillis)) {
            return ResponseEntity.status(403).build()
        }

        return ResponseEntity.ok(CommandResponse("Kotlin is still fun"))
    }
}