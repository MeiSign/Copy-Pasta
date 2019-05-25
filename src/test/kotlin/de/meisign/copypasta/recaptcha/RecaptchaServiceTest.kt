package de.meisign.copypasta.recaptcha

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.net.URI

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class RecaptchaServiceTest {
  private val restTemplate = mock(RestTemplate::class.java)
  private val scoreThreshold = 0.5
  private val secret = "secret"
  private val googleApiUrl = "verify.url"

  private val recaptchaService = RecaptchaService(
    restTemplate,
    scoreThreshold,
    secret,
    googleApiUrl
  )

  private val token = "abc"
  private val verifyUrl = URI.create("$googleApiUrl?secret=$secret&response=$token")

  @Test
  fun isTrustworthyShouldReturnTrueForInvalidResponse() {
    given(restTemplate.getForObject(verifyUrl, RecaptchaResponse::class.java))
      .willReturn(null)

    assertTrue(recaptchaService.isTrustworthy(token))
  }

  @Test
  fun isTrustworthyShouldReturnTrueIfApiCallThrowsException() {
    given(restTemplate.getForObject(verifyUrl, RecaptchaResponse::class.java))
      .willThrow(RestClientException("foo"))

    assertTrue(recaptchaService.isTrustworthy(token))
  }

  @Test
  fun isTrustworthyShouldReturnFalseForScoresLowerThanThreshold() {
    given(restTemplate.getForObject(verifyUrl, RecaptchaResponse::class.java))
      .willReturn(buildRecaptchaResponse(scoreThreshold - 0.1))

    assertFalse(recaptchaService.isTrustworthy(token))
  }

  @Test
  fun isTrustworthyShouldReturnTrueForScoresHigherThanThreshold() {
    given(restTemplate.getForObject(verifyUrl, RecaptchaResponse::class.java))
      .willReturn(buildRecaptchaResponse(scoreThreshold + 0.1))

    assertTrue(recaptchaService.isTrustworthy(token))
  }

  @Test
  fun isTrustworthyShouldReturnTrueIfInputSecretIsWrong() {
    given(restTemplate.getForObject(verifyUrl, RecaptchaResponse::class.java))
      .willReturn(buildRecaptchaResponse(scoreThreshold + 0.1, listOf("invalid-input-secret")))

    assertTrue(recaptchaService.isTrustworthy(token))
  }

  @Test
  fun isTrustworthyShouldReturnTrueIfInputSecretIsMissing() {
    given(restTemplate.getForObject(verifyUrl, RecaptchaResponse::class.java))
      .willReturn(buildRecaptchaResponse(scoreThreshold + 0.1, listOf("missing-input-secret")))

    assertTrue(recaptchaService.isTrustworthy(token))
  }

  @Test
  fun isTrustworthyShouldReturnFalseForClientErrors() {
    given(restTemplate.getForObject(verifyUrl, RecaptchaResponse::class.java))
      .willReturn(buildRecaptchaResponse(scoreThreshold + 0.1, listOf("timeout-or-duplicate")))

    assertFalse(recaptchaService.isTrustworthy(token))
  }

  private fun buildRecaptchaResponse(score: Double, errorCodes: List<String> = emptyList()) =
    RecaptchaResponse(
      errorCodes.isEmpty(),
      score,
      "foo",
      "bar",
      "host",
      errorCodes
    )
}
