package de.meisign.copypasta.storage.filesystem

import com.amazonaws.util.IOUtils
import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.FileStorage
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.WritableResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@Profile("local")
@Component
class FileSystemStorage(private val resourceLoader: FileSystemResourceLoader = FileSystemResourceLoader()) : FileStorage {

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
    if (!resourceLoader.getResource(getFilePath(pointer).parent.toString()).exists())
      Files.createDirectories(rootLocation.resolve(pointer.uuid.toString()))
    val resource = resourceLoader.getResource(getFilePath(pointer).toString()) as WritableResource

    resource.outputStream.use { out ->
      file.inputStream.use {
        log.info("Storing file $pointer to upload-dir")
        it.copyTo(out)
      }
    }

    return pointer
  }

  override fun downloadFile(pointer: FilePointer): ByteArray {
    val resource = resourceLoader.getResource(getFilePath(pointer).toString())

    resource.inputStream.use {
      log.info("Retrieving file $pointer from upload-dir")
      return IOUtils.toByteArray(it)
    }
  }
}
