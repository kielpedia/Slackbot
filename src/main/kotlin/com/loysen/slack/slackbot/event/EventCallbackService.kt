package com.loysen.slack.slackbot.event

import org.springframework.stereotype.Service

@Service
class EventCallbackService {

    fun handleCallback(event: SlackEvent?): EventResponse {
        if ("message" == event?.type) {
            if ("Kotlin".toRegex().containsMatchIn(event.text ?: "")) {

            }
        }

        return EventResponse(null)
    }
}