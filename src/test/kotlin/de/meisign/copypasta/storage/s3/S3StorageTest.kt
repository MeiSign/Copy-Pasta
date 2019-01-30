package de.meisign.copypasta.storage.s3

import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.StorageException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.*
import org.mockito.Mockito.mock
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.ResourceLoader
import org.springframework.mock.web.MockMultipartFile
import java.io.OutputStream
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class S3StorageTest {
  private val resourceLoader: ResourceLoader = mock(DefaultResourceLoader::class.java)
  private val resource = mock(FileSystemResource::class.java)
  private val outputStream = mock(OutputStream::class.java)
  private val service = S3Storage(resourceLoader, "bucketName")

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
      service.storeFile(file)
    }
  }

  @Test
  fun storeFileShouldUploadFileAndReturnFilepointer() {
    val file = MockMultipartFile("name", "original", null, "testContent".toByteArray())
    given(resourceLoader.getResource(ArgumentMatchers.anyString())).willReturn(resource)
    given(resource.outputStream).willReturn(outputStream)
    val pointer = service.storeFile(file)

    verify(outputStream, times(1)).write("testContent".toByteArray().copyOf(DEFAULT_BUFFER_SIZE), 0, 11)
    assertThat(pointer.key, `is`("original"))
  }

  @Test
  fun downloadFile() {
    val pointer = FilePointer(UUID.randomUUID(), "key")
    given(resourceLoader.getResource(ArgumentMatchers.anyString())).willReturn(resource)
    given(resource.inputStream).willReturn("bla".byteInputStream())

    assertThat(service.downloadFile(pointer), `is`("bla".toByteArray()))
  }
}
