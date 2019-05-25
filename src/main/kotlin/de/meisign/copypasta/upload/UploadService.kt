package de.meisign.copypasta.upload

import de.meisign.copypasta.recaptcha.LowRecaptchaScoreException
import de.meisign.copypasta.recaptcha.RecaptchaService
import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.FileStorage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Component
class UploadService(@Autowired private val storage: FileStorage,
                    @Autowired private val recaptchaService: RecaptchaService) {
  suspend fun upload(file: MultipartFile, recaptchaToken: String, uuidParam: UUID?): FilePointer {
    val uuid = uuidParam ?: UUID.randomUUID()
    if (recaptchaService.isTrustworthy(recaptchaToken))
      return storage.storeFile(file, uuid)
    else
      throw LowRecaptchaScoreException()
  }
}
