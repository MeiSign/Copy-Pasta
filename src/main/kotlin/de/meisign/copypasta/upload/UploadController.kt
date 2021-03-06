package de.meisign.copypasta.upload

import de.meisign.copypasta.util.toDeferredResult
import de.meisign.copypasta.storage.FilePointer
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
class UploadController(@Autowired private val uploadService: UploadService) {

  @ExperimentalCoroutinesApi
  @PostMapping("/upload")
  fun upload(@RequestParam("file") file: MultipartFile,
             @RequestParam("recaptchaToken") recaptchaToken: String,
             @RequestParam("uuid", required = false) uuidParam: UUID?): DeferredResult<FilePointer> {
    return GlobalScope.async {
      return@async uploadService.upload(file, recaptchaToken, uuidParam)
    }.toDeferredResult()
  }
}
