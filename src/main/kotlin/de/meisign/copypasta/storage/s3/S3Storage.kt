package de.meisign.copypasta.storage.s3

import de.meisign.copypasta.storage.FileNotFoundException
import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.FileStorage
import de.meisign.copypasta.storage.StorageException
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.*
import java.lang.Exception
import java.util.*

@Component
class S3Storage(@Autowired private val amazonS3: S3AsyncClient,
                @Value("\${aws.s3.bucketName}") private val bucketName: String,
                @Value("\${aws.s3.pollingIntervalMs}") private val pollingInterval: Long,
                @Value("\${aws.s3.pollingRetries}") private val pollingRetries: Int
) : FileStorage {

  private val log = LoggerFactory.getLogger(S3Storage::class.java)

  internal fun getFileName(file: MultipartFile) =
      file.originalFilename ?: if (file.name.isEmpty()) "file" else file.name

  override suspend fun storeFile(file: MultipartFile, uuid: UUID): FilePointer {
    val pointer = FilePointer(uuid, getFileName(file))
    log.info("Storing file $pointer to s3")
    try {
      uploadFileToS3(file, pointer)
      return pointer
    } catch (e: Exception) {
      log.error("[${pointer.path}] Could not upload file", e)
      throw StorageException("Could not upload file")
    }
  }

  private suspend fun uploadFileToS3(file: MultipartFile, pointer: FilePointer) {
    file.inputStream.use {
      amazonS3
        .putObject(
            buildS3PutObjectRequest(pointer),
            AsyncRequestBody.fromBytes(it.readBytes())).await()
    }
  }

  override suspend fun downloadFile(pointer: FilePointer): Resource {
    log.info("[${pointer.path}] Downloading file from s3.")
    try {
      val file = downloadFileFromS3(pointer)
      return ByteArrayResource(file)
    } catch (e: NoSuchKeyException) {
      log.info("[${pointer.path}] S3 File not found.")
      throw FileNotFoundException("File with key ${pointer.key} does not exist.")
    } catch (e: Exception) {
      log.error("[${pointer.path}] Exception during download.", e)
      throw StorageException("Could not download file.")
    }
  }

  private suspend fun downloadFileFromS3(pointer: FilePointer): ByteArray {
    return amazonS3
        .getObject(
            buildS3GetObjectRequest(pointer),
            AsyncResponseTransformer.toBytes()
        ).await().asByteArray()
  }

  override suspend fun awaitDownload(uuid: UUID): FilePointer {
    log.info("[$uuid] Awaiting Download")
    for (i in 1..pollingRetries) {
      log.info("[Try $i][$uuid] Searching file")
      try {
        return searchS3File(uuid)
      } catch (e: FileNotFoundException) {
        delay(pollingInterval)
      } catch (e: Exception) {
        log.error("[$uuid] Could not await download", e)
        throw StorageException("Could not await download")
      }
    }
    throw FileNotFoundException()

  }

  private suspend fun searchS3File(uuid: UUID): FilePointer {
     return amazonS3
        .listObjects(buildS3ListObjectsRequest(uuid))
        .thenApply { listObjResponse ->
          val list = listObjResponse.contents()
          when {
            list.size > 1 -> throw StorageException("Ambigious uuid. More than one file find.")
            list.size == 0 -> throw FileNotFoundException()
            else -> {
              val s3key = extractFileName(list.first(), uuid)
              return@thenApply FilePointer(uuid, s3key)
            }
          }
        }.await()
  }

  internal fun extractFileName(s3Object: S3Object, uuid: UUID) =
    s3Object.key().drop(uuid.toString().length + 1)

  internal fun buildS3ListObjectsRequest(uuid: UUID): ListObjectsRequest =
    ListObjectsRequest
        .builder()
        .bucket(bucketName)
        .prefix(uuid.toString())
        .build()

  internal fun buildS3PutObjectRequest(pointer: FilePointer): PutObjectRequest =
    PutObjectRequest
        .builder()
        .bucket(bucketName)
        .key(pointer.path)
        .build()

  internal fun buildS3GetObjectRequest(pointer: FilePointer): GetObjectRequest =
    GetObjectRequest
        .builder()
        .bucket(bucketName)
        .key(pointer.path)
        .build()
}
