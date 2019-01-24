package de.meisign.copypasta.storage

import org.springframework.web.multipart.MultipartFile
import java.util.*

interface FileStorage {
  fun findFile(uuid: UUID): FilePointer?
  fun storeFile(file: MultipartFile): UUID?
}
