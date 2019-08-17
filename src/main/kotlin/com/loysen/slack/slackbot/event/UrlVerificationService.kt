package com.loysen.slack.slackbot.event

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UrlVerificationService @Autowired constructor(val slackProperties: SlackProperties) {

    fun verifyToken(message: EventMessage): EventResponse {
        if (slackProperties.verificationToken == message.token && message.challenge != null) {
            return EventResponse(message.challenge)
        }

        return EventResponse(null)
    }
}