package de.meisign.copypasta.storage

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class StorageException(message: String = ""): RuntimeException(message)
