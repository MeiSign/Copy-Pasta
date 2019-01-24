package de.meisign.copypasta.storage

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
class StorageException(): RuntimeException()
