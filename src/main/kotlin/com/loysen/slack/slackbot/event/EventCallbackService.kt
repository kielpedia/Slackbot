package com.loysen.slack.slackbot.event

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EventCallbackService @Autowired constructor(val slackProperties: SlackProperties){

    fun handleCallback(event: SlackEvent?): EventResponse {
        if ("message" == event?.type) {
            val inputText = event.text ?: ""
            if (slackProperties.messageTrigger.toRegex().containsMatchIn(inputText)) {

            }
        }

        return EventResponse(null)
    }
}