package de.meisign.copypasta.controller

import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.FileStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.DeferredResult
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
class UploadController(@Autowired private val storage: FileStorage) {

  @ExperimentalCoroutinesApi
  @PostMapping("/upload")
  fun upload(@RequestParam("file") file: MultipartFile,
             @RequestParam("uuid", required = false) uuidParam: UUID?): DeferredResult<FilePointer> {
    return GlobalScope.async {
      val uuid = uuidParam ?: UUID.randomUUID()
      return@async storage.storeFile(file, uuid)
    }.toDeferredResult()
  }
}
