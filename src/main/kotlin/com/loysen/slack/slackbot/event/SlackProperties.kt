package com.loysen.slack.slackbot.event

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("slack")
class SlackProperties {
    var verificationToken: String = "randomToken"

    var messageTrigger: String = "kotlin"
}