package com.loysen.slack.slackbot.event

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI


private val logger = KotlinLogging.logger {}

@Service
class EventCallbackService @Autowired constructor(val slackProperties: SlackProperties, val restTemplate: RestTemplate) {

    fun handleCallback(event: SlackEvent?) {
        GlobalScope.launch {
            logger.info { "received event: type=${event?.type} text=${event?.text}" }

            if ("message" == event?.type) {
                val inputText = event.text ?: ""
                if (slackProperties.messageTrigger.toRegex().containsMatchIn(inputText)) {
                    logger.info { "sending message back to slack" }
                    sendMessageToSlack(event)
                }
            }
        }
    }

    private fun sendMessageToSlack(event: SlackEvent) {
        val uri = URI(slackProperties.slackMessagePostUrl)

        val response: ResponseEntity<Void> = restTemplate.postForEntity(
                uri,
                CreateMessage(token = slackProperties.messageToken, channel = event.channel, text = "Kotlin is fun"),
                Void::class.java)

        if (response.statusCode.isError) {
            logger.error { "Unable to post message back to slack, statusCode=${response.statusCodeValue}" }
        }
    }
}