package com.loysen.slack.slackbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SlackbotApplication

fun main(args: Array<String>) {
	runApplication<SlackbotApplication>(*args)
}
