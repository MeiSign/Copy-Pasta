package de.meisign.copypasta.storage

import com.amazonaws.services.s3.model.S3Object
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.mock.web.MockMultipartFile
import java.util.*

class S3StorageTest {

  @Test
  fun getFileNameReturnsOriginalNameIfDefined() {
    val service = S3Storage(mock(AmazonClient::class.java))
    val file = MockMultipartFile("name", "original", null, null)
    assertThat(service.getFileName(file), `is`("original"))
  }

  @Test
  fun storeFileShouldUploadFileAndReturnFilepointer() {
    val amazonClient = mock(AmazonClient::class.java)
    val service = S3Storage(amazonClient)
    val file = MockMultipartFile("name", "original", null, null)
    val pointer = service.storeFile(file)

    assertThat(pointer.key, `is`("original"))
  }

  @Test
  fun downloadFile() {
    val amazonClient = mock(AmazonClient::class.java)
    val service = S3Storage(amazonClient)
    val pointer = FilePointer(UUID.randomUUID(), "key")
    val obj = S3Object()
    obj.setObjectContent("bla".byteInputStream())

    given(amazonClient.downloadFile(pointer)).willReturn(obj)
    assertThat(String(service.downloadFile(pointer)), `is`("bla"))
  }
}
