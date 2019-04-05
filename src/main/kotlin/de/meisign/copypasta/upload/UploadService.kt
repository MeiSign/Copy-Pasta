package de.meisign.copypasta.upload

import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.FileStorage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Component
class UploadService(@Autowired private val storage: FileStorage) {
  suspend fun upload(file: MultipartFile, recaptchaToken: String, uuidParam: UUID?): FilePointer {
    val uuid = uuidParam ?: UUID.randomUUID()
    return storage.storeFile(file, uuid)
  }
}
