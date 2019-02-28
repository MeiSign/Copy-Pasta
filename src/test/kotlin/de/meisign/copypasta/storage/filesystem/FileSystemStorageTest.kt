package de.meisign.copypasta.storage.filesystem

import de.meisign.copypasta.storage.FileNotFoundException
import de.meisign.copypasta.storage.FilePointer
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.core.io.FileSystemResource
import org.springframework.mock.web.MockMultipartFile
import java.io.OutputStream
import java.nio.file.Paths
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FileSystemStorageTest {
  private val resourceLoader = Mockito.mock(FileSystemStorageLoader::class.java)
  private val resource = Mockito.mock(FileSystemResource::class.java)
  private val outputStream = Mockito.mock(OutputStream::class.java)
  private val pollingRetries = 3

  private val service = FileSystemStorage(resourceLoader, 10, pollingRetries)

  @Test
  fun getFileNameReturnsOriginalNameIfDefined() {
    val file = MockMultipartFile("name", "original", null, null)
    assertThat(service.getFileName(file), Matchers.`is`("original"))
  }

  @Test
  fun getFilePathReturnsCorrectPath() {
    val pointer = FilePointer(UUID.fromString("31f9e985-72b8-4ca7-8a64-607cec211ecd"), "test.jpg")
    assertThat(service.getFilePath(pointer), Matchers.`is`(Paths.get("upload-dir", pointer.uuid.toString(), pointer.key)))
  }


  @Test
  fun storeFileShouldStoreFile() {
    val file = MockMultipartFile("name", "original", null, "testContent".toByteArray())
    given(resourceLoader.getResource(any())).willReturn(resource)
    given(resourceLoader.getResource(any()).exists()).willReturn(true)
    given(resource.outputStream).willReturn(outputStream)
    val pointer = service.storeFile(file, UUID.randomUUID())

    verify(outputStream, times(1)).write("testContent".toByteArray().copyOf(DEFAULT_BUFFER_SIZE), 0, 11)
    assertThat(pointer.key, Matchers.`is`("original"))
  }

  @Test
  fun downloadFileShouldDownloadFile() {
    val pointer = FilePointer(UUID.randomUUID(), "key")
    given(resourceLoader.getResource(any())).willReturn(resource)
    given(resource.exists()).willReturn(true)
    given(resource.inputStream).willReturn("bla".byteInputStream())

    assertThat(service.downloadFile(pointer).inputStream.readBytes(), Matchers.`is`("bla".toByteArray()))
  }

  @Test
  fun awaitDownloadShouldRetryIfItCantFindTheFolder() {
    val uuid = UUID.randomUUID()
    given(resourceLoader.getResource(any())).willReturn(resource)
    given(resource.exists()).willReturn(false)

    assertThrows<FileNotFoundException> {
      runBlocking { service.awaitDownloadAsync(uuid).await() }
    }
    verify(resourceLoader, times(pollingRetries)).getResource(Paths.get("upload-dir", uuid.toString()))
  }

  @Test
  fun awaitDownloadShouldReturnFilePointerForFolderAndFile() {
    val uuid = UUID.randomUUID()
    given(resourceLoader.getResource(any())).willReturn(resource)
    given(resource.exists()).willReturn(true)
    given(resource.filename).willReturn("fileName.jpg")
    given(resourceLoader.getResources(any())).willReturn(Array(1) {resource})


    assertThat(runBlocking { service.awaitDownloadAsync(uuid).await() }, Matchers.`is`(FilePointer(uuid, "fileName.jpg")))
    verify(resourceLoader, times(1)).getResource(Paths.get("upload-dir", uuid.toString()))
  }

  private fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
  }
  private fun <T> uninitialized(): T = null as T
}
