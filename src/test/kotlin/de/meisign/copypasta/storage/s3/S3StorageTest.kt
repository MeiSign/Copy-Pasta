package de.meisign.copypasta.storage.s3

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3ObjectSummary
import de.meisign.copypasta.storage.FileNotFoundException
import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.StorageException
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.*
import org.mockito.Mockito.mock
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.mock.web.MockMultipartFile
import java.io.OutputStream
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class S3StorageTest {
  private val resourceLoader: ResourcePatternResolver = mock(ClassPathXmlApplicationContext::class.java)
  private val resource = mock(FileSystemResource::class.java)
  private val outputStream = mock(OutputStream::class.java)
  private val amazonS3 = mock(AmazonS3Client::class.java)
  private val pollingRetries = 3
  private val bucketName = "bucketName"

  private val service = S3Storage(resourceLoader, amazonS3, bucketName, 10, pollingRetries)

  @Test
  fun getFileNameReturnsOriginalNameIfDefined() {
    val file = MockMultipartFile("name", "original", null, null)
    assertThat(service.getFileName(file), `is`("original"))
  }

  @Test
  fun getS3PathReturnsCorrectS3Uri() {
    val pointer = FilePointer(UUID.fromString("31f9e985-72b8-4ca7-8a64-607cec211ecd"), "test.jpg")
    assertThat(service.getS3Path(pointer), `is`("s3://bucketName/${pointer.uuid}/${pointer.key}"))
  }

  @Test
  fun storeFileShouldCatchCastExceptions() {
    val file = MockMultipartFile("name", "original", null, "testContent".toByteArray())
    given(resourceLoader.getResource(ArgumentMatchers.anyString())).willReturn(null)

    assertThrows<StorageException> {
      service.storeFile(file, UUID.randomUUID())
    }
  }

  @Test
  fun storeFileShouldUploadFileAndReturnFilepointer() {
    val file = MockMultipartFile("name", "original", null, "testContent".toByteArray())
    given(resourceLoader.getResource(ArgumentMatchers.anyString())).willReturn(resource)
    given(resource.outputStream).willReturn(outputStream)
    val pointer = service.storeFile(file, UUID.randomUUID())

    verify(outputStream, times(1)).write("testContent".toByteArray().copyOf(DEFAULT_BUFFER_SIZE), 0, 11)
    assertThat(pointer.key, `is`("original"))
  }

  @Test
  fun downloadFileShouldReturnResource() {
    val pointer = FilePointer(UUID.randomUUID(), "key")
    given(resourceLoader.getResource(ArgumentMatchers.anyString())).willReturn(resource)
    given(resource.inputStream).willReturn("bla".byteInputStream())
    given(resource.exists()).willReturn(true)

    assertThat(service.downloadFile(pointer).inputStream.readBytes(), `is`("bla".toByteArray()))
  }

  @Test
  fun downloadFileShouldThrowFileNotFoundIfFileDoesNotExist() {
    val pointer = FilePointer(UUID.randomUUID(), "key")
    given(resourceLoader.getResource(ArgumentMatchers.anyString())).willReturn(resource)
    given(resource.inputStream).willReturn("bla".byteInputStream())
    given(resource.exists()).willReturn(false)

    assertThrows<FileNotFoundException> {
      service.downloadFile(pointer).inputStream.readBytes()
    }
  }

  @Test
  fun awaitDownloadShouldRetryIfItCantFindTheFolder() {
    val uuid = UUID.randomUUID()
    given(amazonS3.listObjects(bucketName, uuid.toString())).willReturn(ObjectListing())

    assertThrows<FileNotFoundException> {
      runBlocking { service.awaitDownloadAsync(uuid).await() }
    }
    verify(amazonS3, times(pollingRetries)).listObjects(bucketName, uuid.toString())
  }

  @Test
  fun awaitDownloadShouldReturnFilePointerForS3FolderAndFile() {
    val uuid = UUID.randomUUID()
    val folder = createObjectSummary(bucketName, uuid.toString(), 0L)
    val file = createObjectSummary(bucketName, "fileName.jpg", 22L)

    val objectListing = ObjectListing()
    objectListing.objectSummaries.add(folder)
    objectListing.objectSummaries.add(file)
    given(amazonS3.listObjects(bucketName, uuid.toString())).willReturn(objectListing)

    assertThat(runBlocking { service.awaitDownloadAsync(uuid).await() }, `is`(FilePointer(uuid, "fileName.jpg")))
    verify(amazonS3, times(1)).listObjects(bucketName, uuid.toString())
  }

  private fun createObjectSummary(bucketName: String, key: String, size: Long): S3ObjectSummary {
    val file = S3ObjectSummary()
    file.size = size
    file.key = key
    file.bucketName = bucketName

    return file
  }
}
