package de.meisign.copypasta.storage

import com.amazonaws.util.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Component
class S3Storage(@Autowired private val amazonClient: AmazonClient) : FileStorage {

  fun getFileName(file: MultipartFile): String = file.originalFilename
      ?: if (file.name.isEmpty()) "file" else file.name

  override fun storeFile(file: MultipartFile): FilePointer {
    val filePointer = FilePointer(UUID.randomUUID(), getFileName(file))
    file.inputStream.use {
      amazonClient.uploadFile(filePointer.path(), it)
    }

    return filePointer;
  }

  override fun downloadFile(pointer: FilePointer): ByteArray {
    amazonClient.downloadFile(pointer).objectContent.use {
      return IOUtils.toByteArray(it)
    }
  }

}
