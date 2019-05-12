package com.loysen.slack.slackbot.web

import com.loysen.slack.slackbot.event.EventCallbackService
import com.loysen.slack.slackbot.event.EventResponse
import com.loysen.slack.slackbot.event.SlackMessage
import com.loysen.slack.slackbot.event.UrlVerificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
class EventHandlerController @Autowired constructor(private val urlVerificationService: UrlVerificationService,
                                                    private val eventCallbackService: EventCallbackService) {

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun handleSlackEvent(@Valid @RequestBody message: SlackMessage,
                         @RequestHeader(name = "X-Slack-Retry-Num", required = false) requestCount: Int?): EventResponse {

        if (message.type == "url_verification") {
            return urlVerificationService.verifyToken(message)
        } else if (message.type == "event_callback" &&  requestCount == null) {
            eventCallbackService.handleCallback(message.event)
        }

        return EventResponse(null)
    }
}