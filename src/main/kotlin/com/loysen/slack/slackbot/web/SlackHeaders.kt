package com.loysen.slack.slackbot.web

class SlackHeaders {
    companion object {
        const val NUM_RETRIES = "X-Slack-Retry-Num"
        const val SIGNATURE = "X-Slack-Signature"
        const val REQUEST_TIME = "X-Slack-Request-Timestamp"
    }
}