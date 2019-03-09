package de.meisign.copypasta.storage.s3

import de.meisign.copypasta.storage.FileNotFoundException
import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.StorageException
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.*
import java.util.*
import java.util.concurrent.CompletableFuture

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class S3StorageTest {
  private val amazonS3 = mock(S3AsyncClient::class.java)
  private val pollingRetries = 3
  private val bucketName = "bucketName"
  private val service = S3Storage(amazonS3, bucketName, 10, pollingRetries)

  private val uuid: UUID = UUID.randomUUID()
  private val fileName = "fileName"
  private val fileContent = "testContent"
  private val file = MockMultipartFile("name", fileName , null, fileContent.toByteArray())

  @Test
  fun storeFileShouldCatchExceptions() {
    val putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key("$uuid/$fileName").build()
    val asyncRequestBody = AsyncRequestBody.fromBytes(fileContent.toByteArray())
    given(amazonS3.putObject(putObjectRequest, asyncRequestBody)).willThrow(RuntimeException("aws broken"))

    assertThrows<StorageException> {
      runBlocking {
        return@runBlocking service.storeFile(file, uuid)
      }
    }
  }

  @Test
  fun downloadFileShouldCatchNoSuchKeyExceptions() {
    given(amazonS3.getObject(any<GetObjectRequest>(), any<AsyncResponseTransformer<GetObjectResponse, GetObjectRequest>>())).willThrow(NoSuchKeyException.builder().build())

    assertThrows<FileNotFoundException> {
      runBlocking {
        return@runBlocking service.downloadFile(FilePointer(uuid, fileName))
      }
    }
  }

  @Test
  fun downloadFileShouldCatchOtherExceptions() {
    given(amazonS3.getObject(any<GetObjectRequest>(), any<AsyncResponseTransformer<GetObjectResponse, GetObjectRequest>>())).willThrow(RuntimeException())

    assertThrows<StorageException> {
      runBlocking {
        return@runBlocking service.downloadFile(FilePointer(uuid, fileName))
      }
    }
  }

  @Test
  fun awaitDownloadShouldRetryIfItCantFindTheFolder() {
    given(amazonS3.listObjects(any<ListObjectsRequest>())).willReturn(CompletableFuture.completedFuture(ListObjectsResponse.builder().build()))

    assertThrows<FileNotFoundException> {
      runBlocking { service.awaitDownload(uuid) }
    }
    val listRequest = ListObjectsRequest.builder().bucket(bucketName).prefix(uuid.toString()).build()
    verify(amazonS3, times(pollingRetries)).listObjects(listRequest)
  }

  @Test
  fun awaitDownloadShouldCatchExceptions() {
    given(amazonS3.listObjects(any<ListObjectsRequest>())).willThrow(RuntimeException())

    assertThrows<StorageException> {
      runBlocking { service.awaitDownload(uuid) }
    }
  }

  @Test
  fun getFileNameReturnsOriginalNameIfDefined() {
    assertThat(service.getFileName(file), `is`(fileName))
  }

  @Test
  fun extractFileNameFromS3ObjectCorrectly() {
    val name1 = service
      .extractFileName(
        S3Object.builder().key("$uuid/name/name.jpg").build(),
        uuid)

    val name2 = service
      .extractFileName(
        S3Object.builder().key("$uuid/name.jpg").build(),
        uuid)

    assertThat(name1, `is`("name/name.jpg"))
    assertThat(name2, `is`("name.jpg"))
  }

  @Test
  fun buildS3ListObjectsRequestsCorrectly() {
    val request = service.buildS3ListObjectsRequest(uuid)

    assertThat(request.bucket(), `is`(bucketName))
    assertThat(request.prefix(), `is`("$uuid"))
  }

  @Test
  fun buildS3PutObjectRequestCorrectly() {
    val pointer = FilePointer(uuid, fileName)
    val request = service.buildS3PutObjectRequest(pointer)

    assertThat(request.bucket(), `is`(bucketName))
    assertThat(request.key(), `is`(pointer.path))
  }

  @Test
  fun buildS3GetObjectRequestCorrectly() {
    val pointer = FilePointer(uuid, fileName)
    val request = service.buildS3GetObjectRequest(pointer)

    assertThat(request.bucket(), `is`(bucketName))
    assertThat(request.key(), `is`(pointer.path))
  }

  private fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
  }

  private fun <T> eq(t: T): T {
    Mockito.eq<T>(t)
    return uninitialized()
  }
  private fun <T> uninitialized(): T = null as T

}
