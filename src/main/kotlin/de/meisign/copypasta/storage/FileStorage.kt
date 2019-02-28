package de.meisign.copypasta.storage

import kotlinx.coroutines.Deferred
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface FileStorage {
  fun storeFile(file: MultipartFile, uuid: UUID): FilePointer
  fun downloadFile(pointer: FilePointer): Resource
  fun awaitDownloadAsync(uuid: UUID): Deferred<FilePointer>
}
