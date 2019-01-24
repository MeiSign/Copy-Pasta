package de.meisign.copypasta.controller

import de.meisign.copypasta.storage.FileStorage
import de.meisign.copypasta.storage.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.net.URLConnection.guessContentTypeFromStream
import java.util.*


@RestController
class DownloadController(@Autowired private val storage: FileStorage) {

  private val log = LoggerFactory.getLogger(DownloadController::class.java)


  @GetMapping(value = "/download/{uuid}")
  fun download(@PathVariable uuid: UUID): ResponseEntity<Resource> {
    storage.findFile(uuid)?.let {
      val length = it.length()
      it.stream()?.let {
        log.info("Serving {} with Content Type {} and length {}", uuid.toString(), guessContentTypeFromStream(it), length)
        return ResponseEntity
            .ok()
            .contentLength(length)
            .header("Content-Type", guessContentTypeFromStream(it))
            .body(InputStreamResource(it))
      }
    } ?: throw NotFoundException()
  }
}

