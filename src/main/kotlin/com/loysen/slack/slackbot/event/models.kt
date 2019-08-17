package com.loysen.slack.slackbot.event

data class EventResponse(val challenge: String?)

data class EventMessage(val token: String, val challenge: String? = null, val type: String, val event: EventDetails? = null)

data class EventDetails(val type: String, val channel: String, val user: String? = null, val text: String?, val subtype: String? = null)

data class CreateMessage(val token: String, val channel: String, val text: String)

data class CommandResponse(val text:String)
