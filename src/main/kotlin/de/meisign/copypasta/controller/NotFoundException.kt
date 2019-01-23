package de.meisign.copypasta.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(value= HttpStatus.NOT_FOUND)
class NotFoundException: RuntimeException()
