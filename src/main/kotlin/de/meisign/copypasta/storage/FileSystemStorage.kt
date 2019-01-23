package de.meisign.copypasta.storage

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.net.URL
import java.util.*

@Component
class FileSystemStorage : FileStorage {
  private val log = LoggerFactory.getLogger(FileSystemStorage::class.java)


  override fun findFile(uuid: UUID): FilePointer? {
    log.info("Searching file {}", uuid)
    val resource: URL? = FileSystemStorage::class.java.getResource(uuid.toString())

    return resource?.let {
      log.info("Filepath {}", it.path)
      File(it.file)
    }?.let {
      log.info("File {}", it.absolutePath)
      FileSystemPointer(it)
    }
  }
}
