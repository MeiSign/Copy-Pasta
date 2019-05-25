package de.meisign.copypasta.recaptcha

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN)
class LowRecaptchaScoreException(message: String = "") : RuntimeException(message)
