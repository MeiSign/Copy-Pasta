package de.meisign.copypasta.storage.filesystem

import de.meisign.copypasta.storage.FilePointer
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.mock.web.MockMultipartFile
import java.io.OutputStream
import java.nio.file.Paths
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FileSystemStorageTest {
  private val resourceLoader = Mockito.mock(FileSystemResourceLoader::class.java)
  private val resource = Mockito.mock(FileSystemResource::class.java)
  private val outputStream = Mockito.mock(OutputStream::class.java)
  private val service = FileSystemStorage(resourceLoader)

  @Test
  fun getFileNameReturnsOriginalNameIfDefined() {
    val file = MockMultipartFile("name", "original", null, null)
    MatcherAssert.assertThat(service.getFileName(file), Matchers.`is`("original"))
  }

  @Test
  fun getFilePathReturnsCorrectPath() {
    val pointer = FilePointer(UUID.fromString("31f9e985-72b8-4ca7-8a64-607cec211ecd"), "test.jpg")
    MatcherAssert.assertThat(service.getFilePath(pointer), Matchers.`is`(Paths.get("upload-dir", pointer.uuid.toString(), pointer.key)))
  }


  @Test
  fun storeFileShouldStoreFile() {
    val file = MockMultipartFile("name", "original", null, "testContent".toByteArray())
    given(resourceLoader.getResource(ArgumentMatchers.anyString())).willReturn(resource)
    given(resourceLoader.getResource(ArgumentMatchers.anyString()).exists()).willReturn(true)
    given(resource.outputStream).willReturn(outputStream)
    val pointer = service.storeFile(file)

    BDDMockito.verify(outputStream, BDDMockito.times(1)).write("testContent".toByteArray().copyOf(DEFAULT_BUFFER_SIZE), 0, 11)
    MatcherAssert.assertThat(pointer.key, Matchers.`is`("original"))
  }

  @Test
  fun downloadFileShouldDownloadFile() {
    val pointer = FilePointer(UUID.randomUUID(), "key")
    given(resourceLoader.getResource(ArgumentMatchers.anyString())).willReturn(resource)
    given(resource.inputStream).willReturn("bla".byteInputStream())

    MatcherAssert.assertThat(service.downloadFile(pointer), Matchers.`is`("bla".toByteArray()))
  }
}
