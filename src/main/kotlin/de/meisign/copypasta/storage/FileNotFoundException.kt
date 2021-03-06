package de.meisign.copypasta.storage

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(HttpStatus.NOT_FOUND)
class FileNotFoundException(message: String = "") : RuntimeException(message)
