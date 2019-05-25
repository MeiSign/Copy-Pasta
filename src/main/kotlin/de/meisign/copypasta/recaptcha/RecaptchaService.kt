package de.meisign.copypasta.recaptcha

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.net.URI

@Component
class RecaptchaService(
  @Autowired private val recaptchaRestTemplate: RestTemplate,
  @Value("\${recaptcha.scoreThreshold}") private val scoreThreshold: Double,
  @Value("\${recaptcha.secret}") private val recaptchaSecret: String,
  @Value("\${recaptcha.apiUrl}") private val googleVerifyUrl: String
) {

  private val log = LoggerFactory.getLogger(RecaptchaService::class.java)

  fun isTrustworthy(recaptchaToken: String): Boolean {
    val verifyUri = URI.create("$googleVerifyUrl?secret=$recaptchaSecret&response=$recaptchaToken")

    val response: RecaptchaResponse? = try {
      recaptchaRestTemplate.getForObject(verifyUri, RecaptchaResponse::class.java)
    } catch (e: RestClientException) {
      log.error("Google Recaptcha verification failed", e)
      null
    }

    return response?.let { parseResponse(it) } ?: true
  }

  private fun parseResponse(response: RecaptchaResponse): Boolean {
    if (!response.success && response.hasServerError()) {
      log.error("Google ReCaptach response has errors: ${response.errorCodes}")
      return true
    }

    return response.success && response.score >= scoreThreshold
  }
}
