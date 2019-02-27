package de.meisign.copypasta.storage.s3

import com.amazonaws.services.s3.AmazonS3
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
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.WritableResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Profile("!local")
@Component
class S3Storage(@Autowired private val resourceLoader: ResourceLoader,
                @Autowired private val amazonS3: AmazonS3,
                @Value("\${s3.bucketName}") private val bucketName: String,
                @Value("\${s3.pollingIntervalMs}") private val pollingInterval: Long,
                @Value("\${s3.pollingRetries}") private val pollingRetries: Int
) : FileStorage {

  private val log = LoggerFactory.getLogger(S3Storage::class.java)

  fun getFileName(file: MultipartFile): String = file.originalFilename
      ?: if (file.name.isEmpty()) "file" else file.name

  fun getS3Path(pointer: FilePointer) = "s3://$bucketName/${pointer.path()}"

  override fun storeFile(file: MultipartFile): FilePointer {
    val pointer = FilePointer(UUID.randomUUID(), getFileName(file))
    val resource = try {
      resourceLoader.getResource(getS3Path(pointer)) as WritableResource
    } catch (e: ClassCastException) {
      fail(pointer, "Can't cast resource to writable Resource", e)
    }

    resource.outputStream.use { out ->
      file.inputStream.use {
        it.copyTo(out)
      }
    }

    return pointer
  }

  override fun downloadFile(pointer: FilePointer): Resource {
    val resource = resourceLoader.getResource(getS3Path(pointer))
    if (!resource.exists()) throw FileNotFoundException()

    return resource
  }

  fun fail(pointer: FilePointer, message: String, e: Exception): Nothing {
    log.error("Error: $message || FilePointer: $pointer", e)
    throw StorageException()
  }

  override fun awaitDownloadAsync(uuid: UUID): Deferred<FilePointer> {
    log.info("Awaiting Download with prefix $uuid")
    return GlobalScope.async {
      searchS3File(uuid)
    }
  }

  private suspend fun searchS3File(uuid: UUID): FilePointer {
    for (i in 1..pollingRetries) {
      log.info("[Try $i][$uuid] Listing Bucket content")
      val list = amazonS3.listObjects(bucketName, uuid.toString()).objectSummaries
      if (list.size > 3) throw StorageException()
      if (list.size == 2) {
        log.info("[Try $i][$uuid] Bucket folder and file found")
        val key: String? = list.find { it.size != 0L }?.key
        if (key == null) throw FileNotFoundException()
        else {
          val pointer = FilePointer(uuid, key.split("/").last())
          log.info("[Try $i][$uuid] Returning pointer $pointer")
          return pointer
        }
      }
      delay(pollingInterval)
    }
    throw FileNotFoundException()
  }

}
