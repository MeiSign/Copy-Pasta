package de.meisign.copypasta.controller

import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.FileStorage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class DownloadController(@Autowired private val storage: FileStorage) {

  private val log = LoggerFactory.getLogger(DownloadController::class.java)

  @GetMapping("/download/{uuid}/{key}")
  fun download(@PathVariable uuid: UUID, @PathVariable key: String): ResponseEntity<ByteArray> {
    log.info("Downloading {}/{}", uuid.toString(), key)
    val bytes = storage.downloadFile(FilePointer(uuid, key))

    val httpHeaders = HttpHeaders()
    httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM)
    httpHeaders.setContentLength(bytes.size.toLong())
    httpHeaders.setContentDispositionFormData("attachment", key)

    return ResponseEntity(bytes, httpHeaders, HttpStatus.OK)
  }
}

