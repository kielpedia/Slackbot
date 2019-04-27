package com.loysen.slack.slackbot.event

data class EventResponse(val challenge: String?)

data class SlackMessage(val token: String, val challenge: String? = null, val type: String, val event: SlackEvent? = null)

data class SlackEvent(val type: String, val channel: String, val user: String? = null, val text: String?)

data class CreateMessage(val token: String, val channel: String, val text: String)
