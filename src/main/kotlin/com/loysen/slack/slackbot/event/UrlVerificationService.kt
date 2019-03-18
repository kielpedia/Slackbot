package com.loysen.slack.slackbot.event

import org.springframework.stereotype.Service

@Service
class UrlVerificationService {

    val token = "token"

    fun verifyToken(message: SlackMessage): EventResponse {
        if (token == message.token && message.challenge != null) {
            return EventResponse(message.challenge)
        }

        return EventResponse(null)
    }
}