package com.loysen.slack.slackbot.verification

import com.loysen.slack.slackbot.event.SlackProperties
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.joinToString

private val logger = KotlinLogging.logger {}

@Service
class RequestValidator @Autowired constructor(val slackProperties: SlackProperties) {

    fun verifyRequest(signature: String, rawBody: String, requestTime: Long,
                      currentTime: Long): Boolean {
        return isRequestTimeRecent(requestTime, currentTime) && isValidSignature(signature, rawBody, requestTime)
    }

    private fun isRequestTimeRecent(requestTime: Long, currentTime: Long): Boolean {
        val requestInstant = Instant.ofEpochMilli(requestTime)
        val currentTimeInstant = Instant.ofEpochMilli(currentTime)
        val duration = Duration.between(requestInstant, currentTimeInstant).abs().toMinutes()

        if (duration > 5) {
            logger.warn { "request time invalid differenceInMinutes=$duration" }
            return false
        }
        return true
    }

    private fun isValidSignature(requestSignature: String, rawBody: String, requestTime: Long): Boolean {
        val calculatedSignature = createSignature(rawBody, requestTime)
        return requestSignature == calculatedSignature
    }

    private fun createSignature(rawBody: String, requestTime: Long): String {
        val unsignedMessage = "v0:$requestTime:$rawBody"
        val mac = createMac()

        val signed = mac.doFinal(unsignedMessage.toByteArray())
        val hexDigest = signed.joinToString("") { "%02x".format(it) }

        return "v0=$hexDigest"
    }

    private fun createMac(): Mac {
        val hmacType = "HmacSHA256"

        val keySpec = SecretKeySpec(slackProperties.signedSecret.toByteArray(), hmacType)
        val mac = Mac.getInstance(hmacType)
        mac.init(keySpec)

        return mac
    }

}