package com.loysen.slack.slackbot

import com.loysen.slack.slackbot.event.SlackProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(SlackProperties::class)
class SlackbotApplication

fun main(args: Array<String>) {
	runApplication<SlackbotApplication>(*args)
}
