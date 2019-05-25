package de.meisign.copypasta.recaptcha

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class RecaptchaResponse(
  @JsonProperty("success") val success: Boolean,
  @JsonProperty("score") val score: Double,
  @JsonProperty("action", required = false) val action: String?,
  @JsonProperty("challenge_ts", required = false) val challenge: String?,
  @JsonProperty("hostname", required = false) val hostname: String?,
  @JsonProperty("error-codes", required = false) val errorCodes: List<String>?
  ) {

  @JsonIgnore
  fun hasServerError(): Boolean {
    return errorCodes?.let {
      for (error in it) {
        when (error) {
          "invalid-input-secret", "missing-input-secret" -> return@let true
        }
      }
      return@let false
    } ?: false
  }
}
