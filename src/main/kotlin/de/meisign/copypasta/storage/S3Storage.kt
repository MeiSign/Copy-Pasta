package de.meisign.copypasta.storage

import com.amazonaws.util.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.WritableResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.lang.Exception
import java.util.*

@Component
class S3Storage(@Autowired private val resourceLoader: ResourceLoader,
                @Value("\${s3.bucketName}") private val bucketName: String) : FileStorage {

  private val log = LoggerFactory.getLogger(S3Storage::class.java)

  fun getFileName(file: MultipartFile): String = file.originalFilename
      ?: if (file.name.isEmpty()) "file" else file.name

  fun getS3Path(pointer: FilePointer) = "s3://$bucketName/${pointer.path()}"

  override fun storeFile(file: MultipartFile): FilePointer {
    val filePointer = FilePointer(UUID.randomUUID(), getFileName(file))
    val resource = try {
      resourceLoader.getResource(getS3Path(filePointer)) as WritableResource
    } catch (e: ClassCastException) {
      fail(filePointer, "Can't cast resource to writable Resource", e)
    }

    resource.outputStream.use { out ->
      file.inputStream.use {
        it.copyTo(out)
      }
    }

    return filePointer
  }

  override fun downloadFile(pointer: FilePointer): ByteArray {
    val resource = resourceLoader.getResource(getS3Path(pointer))

    resource.inputStream.use {
      return IOUtils.toByteArray(it)
    }
  }

  fun fail(pointer: FilePointer, message: String, e: Exception): Nothing {
    log.error("Error: $message || FilePointer: $pointer", e)
    throw S3StorageException()
  }
}
