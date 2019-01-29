package de.meisign.copypasta.storage

import org.springframework.web.multipart.MultipartFile

interface FileStorage {
  fun storeFile(file: MultipartFile): FilePointer
  fun downloadFile(pointer: FilePointer): ByteArray
}
