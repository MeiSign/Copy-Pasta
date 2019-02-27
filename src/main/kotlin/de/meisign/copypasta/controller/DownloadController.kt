package de.meisign.copypasta.controller

import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.FileStorage
import de.meisign.copypasta.storage.StorageException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.DeferredResult
import java.io.IOException
import java.util.*

@RestController
class DownloadController(@Autowired private val storage: FileStorage) {

  private val log = LoggerFactory.getLogger(DownloadController::class.java)

  @GetMapping("/download/{uuid}/{name}")
  fun download(@PathVariable uuid: UUID, @PathVariable name: String): ResponseEntity<Resource> {
    log.info("Downloading {}/{}", uuid.toString(), name)

    try {
      val resource = storage.downloadFile(FilePointer(uuid, name))

      return serveFile(resource, name)
    } catch (e: IOException) {
      log.error("Storage Exception while opening input stream", e)
      throw StorageException()
    }
  }

  @ExperimentalCoroutinesApi
  @GetMapping("/awaitDownload/{uuid}")
  fun awaitDownload(@PathVariable uuid: UUID): DeferredResult<FilePointer> {
    val result = DeferredResult<FilePointer>()
    val deferred = storage.awaitDownloadAsync(uuid)

    result.onTimeout { deferred.cancel() }

    deferred.invokeOnCompletion {
      exception ->
      if (exception != null) {
        result.setErrorResult(exception)
      } else {
        result.setResult(deferred.getCompleted())
      }
    }

    return result
  }

  @Throws(IOException::class)
  fun serveFile(resource: Resource, name: String): ResponseEntity<Resource> {
    val httpHeaders = HttpHeaders()
    httpHeaders.contentType = MediaType.APPLICATION_OCTET_STREAM
    httpHeaders.setContentDispositionFormData("attachment", name)

    return ResponseEntity
        .status(HttpStatus.OK)
        .headers(httpHeaders)
        .contentLength(resource.contentLength())
        .lastModified(resource.lastModified())
        .body(resource)
  }
}

