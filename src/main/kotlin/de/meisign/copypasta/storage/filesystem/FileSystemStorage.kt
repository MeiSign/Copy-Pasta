package de.meisign.copypasta.storage.filesystem

import de.meisign.copypasta.storage.FileNotFoundException
import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.FileStorage
import de.meisign.copypasta.storage.StorageException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.core.io.WritableResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@Profile("local")
@Component
class FileSystemStorage(@Autowired val fileSystemStorageLoader: FileSystemStorageLoader,
                        @Value("\${s3.pollingIntervalMs}") private val pollingInterval: Long,
                        @Value("\${s3.pollingRetries}") private val pollingRetries: Int
) : FileStorage {

  private val rootLocation = Paths.get("upload-dir")

  private val log = LoggerFactory.getLogger(FileSystemStorage::class.java)

  fun getFileName(file: MultipartFile): String = file.originalFilename
      ?: if (file.name.isEmpty()) "file" else file.name

  fun getFilePath(pointer: FilePointer): Path =
      rootLocation
          .resolve(pointer.uuid.toString())
          .resolve(pointer.key)

  override fun storeFile(file: MultipartFile): FilePointer {
    val pointer = FilePointer(UUID.randomUUID(), getFileName(file))
    if (!fileSystemStorageLoader.getResource(getFilePath(pointer).parent).exists())
      Files.createDirectories(rootLocation.resolve(pointer.uuid.toString()))
    val resource = fileSystemStorageLoader.getResource(getFilePath(pointer)) as WritableResource

    resource.outputStream.use { out ->
      file.inputStream.use {
        log.info("Storing file $pointer to upload-dir")
        it.copyTo(out)
      }
    }

    return pointer
  }

  override fun downloadFile(pointer: FilePointer): Resource {
    val resource = fileSystemStorageLoader.getResource(getFilePath(pointer))
    if (!resource.exists()) throw FileNotFoundException()

    return resource
  }

  override fun awaitDownloadAsync(uuid: UUID): Deferred<FilePointer> {
    log.info("Awaiting Download with prefix $uuid")
    return GlobalScope.async {
      searchFile(uuid)
    }
  }

  private suspend fun searchFile(uuid: UUID): FilePointer {
    val folderPath = rootLocation.resolve(uuid.toString()).resolve("*")
    println(folderPath)
    for (i in 1..pollingRetries) {
      log.info("[Try $i][$uuid] Searching file")
      val resource = fileSystemStorageLoader.getResource(folderPath)
      if (resource.exists()) {
        log.info("[Try $i][$uuid] Found folder")
        val resources = fileSystemStorageLoader.getResources(folderPath)

        if (resources.size > 1) throw StorageException("More than one file found.")
        if (resources.size == 1) {
          log.info("[Try $i][$uuid] Found file")
          val fileName = resources.get(0).filename
          if (fileName == null) throw FileNotFoundException()
          else return FilePointer(uuid, fileName)
        }
      }

      delay(pollingInterval)
    }

    throw FileNotFoundException()
  }

}
