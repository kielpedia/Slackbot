package com.loysen.slack.slackbot.verification

import com.loysen.slack.slackbot.event.SlackProperties
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class RequestValidatorTest {

    private val properties = SlackProperties()
    private val service: RequestValidator = RequestValidator(properties)

    @Before
    fun setup() {
        properties.signedSecret = "8f742231b10e8888abcd99yyyzzz85a5"
    }

    @Test
    fun `should approve valid signature`() {
        val requestTime = 1531420618L
        val currentTime = 1531420618L
        val rawBody = "token=xyzz0WbapA4vBCDEFasx0q6G&team_id=T1DC2JH3J&team_domain=testteamnow&channel_id=G8PSS9T3V&channel_name=foobar&user_id=U2CERLKJA&user_name=roadrunner&command=%2Fwebhook-collect&text=&response_url=https%3A%2F%2Fhooks.slack.com%2Fcommands%2FT1DC2JH3J%2F397700885554%2F96rGlfmibIGlgcZRskXaIFfN&trigger_id=398738663015.47445629121.803a0bc887a14d10d2c447fce8b6703c"
        val providedSignature = "v0=a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503"

        val result = service.verifyRequest(providedSignature, rawBody, requestTime, currentTime)

        assertThat(result, `is`(true))
    }

    @Test
    fun `should reject invalid signature`() {
        val requestTime = 1531420618L
        val currentTime = 1531420618L
        val rawBody = "wrong body"
        val providedSignature = "v0=a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503"

        val result = service.verifyRequest(providedSignature, rawBody, requestTime, currentTime)

        assertThat(result, `is`(false))
    }

    @Test
    fun `should reject requestTime that is over 5 minutes in the past`() {
        val requestTime = ZonedDateTime.of(2015, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        val currentTime = requestTime.plusMinutes(6)
        val rawBody = "token=xyzz0WbapA4vBCDEFasx0q6G&team_id=T1DC2JH3J&team_domain=testteamnow&channel_id=G8PSS9T3V&channel_name=foobar&user_id=U2CERLKJA&user_name=roadrunner&command=%2Fwebhook-collect&text=&response_url=https%3A%2F%2Fhooks.slack.com%2Fcommands%2FT1DC2JH3J%2F397700885554%2F96rGlfmibIGlgcZRskXaIFfN&trigger_id=398738663015.47445629121.803a0bc887a14d10d2c447fce8b6703c"
        val providedSignature = "v0=a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503"

        val result = service.verifyRequest(providedSignature, rawBody, requestTime.toInstant().toEpochMilli(), currentTime.toInstant().toEpochMilli())

        assertThat(result, `is`(false))
    }

}