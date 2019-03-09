package de.meisign.copypasta.storage

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface FileStorage {
  suspend fun storeFile(file: MultipartFile, uuid: UUID): FilePointer
  suspend fun downloadFile(pointer: FilePointer): Resource
  suspend fun awaitDownload(uuid: UUID): FilePointer
}
