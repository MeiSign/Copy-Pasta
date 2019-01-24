package de.meisign.copypasta.storage

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import javax.annotation.PostConstruct


@Component
class FileSystemStorage(val rootLocation: Path = Paths.get("upload-dir")) : FileStorage {
  private val log = LoggerFactory.getLogger(FileSystemStorage::class.java)

  @PostConstruct
  fun init() {
    try {
      log.info("Creating upload dir: {}", rootLocation.toAbsolutePath().toString())
      Files.createDirectories(rootLocation)
    } catch (e: IOException) {
      log.error("Could not initialize storage", e)
    }

  }

  override fun findFile(uuid: UUID): FilePointer? {
    log.info("Searching file {}", rootLocation.resolve(uuid.toString()))
    val resource: Path = rootLocation.resolve(uuid.toString())

    return FileSystemPointer(resource.toFile())
  }

  override fun storeFile(file: MultipartFile): UUID? {
    val uuid = UUID.randomUUID()
    if (file.isEmpty) {
      log.info("File can't be empty")
      return null;
    }
    try {
      file.inputStream.use { inputStream ->
        log.info("Saving File {}", rootLocation.resolve(uuid.toString()))
        Files.copy(inputStream, rootLocation.resolve(uuid.toString()),
            StandardCopyOption.REPLACE_EXISTING)
      }
      return uuid
    } catch (e: IOException) {
      log.info("Failed to store file: {}", e.message)
    }
    return null;
  }
}
