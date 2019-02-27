package de.meisign.copypasta.controller

import de.meisign.copypasta.storage.FileNotFoundException
import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.FileStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders.CONTENT_LENGTH
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.io.IOException
import java.time.Instant
import java.util.*


@ExtendWith(SpringExtension::class)
@WebMvcTest(DownloadController::class)
internal class DownloadControllerTest(@Autowired
                                      private val mvc: MockMvc) {

  @MockBean
  private val storage: FileStorage? = null

  @Test
  fun downloadShouldReturn200ForCorrectUuid() {
    val uuid: UUID = UUID.randomUUID()
    val resource = mock(InputStreamResource::class.java)
    val content = "Hello World"
    val now = Instant.now().toEpochMilli()
    given(resource.contentLength()).willReturn(content.length.toLong())
    given(resource.inputStream).willReturn(content.byteInputStream())
    given(resource.lastModified()).willReturn(now)

    given(storage?.downloadFile(FilePointer(uuid, "key"))).willReturn(resource)

    mvc.perform(get("/download/$uuid/key"))
        .andExpect(status().isOk)
        .andExpect(content().bytes(content.toByteArray()))
        .andExpect(header().longValue(CONTENT_LENGTH, content.toByteArray().size.toLong()))
  }

  @Test
  fun awaitDownloadShouldReturn200ForCorrectUuid() {
    val uuid: UUID = UUID.randomUUID()
    val pointer = FilePointer(uuid, "file.jpg")

    given(storage?.awaitDownloadAsync(uuid)).willReturn(GlobalScope.async { pointer })

    val result = mvc.perform(get("/awaitDownload/$uuid")).andReturn()

    mvc.perform(asyncDispatch(result))
        .andExpect(status().isOk)
        .andExpect(content().string("{\"uuid\":\"" + uuid.toString() + "\",\"key\":\"file.jpg\"}"))

  }

  @Test
  fun awaitDownloadShouldReturn404ForWrongUuid() {
    val uuid: UUID = UUID.randomUUID()

    given(storage?.awaitDownloadAsync(uuid)).willReturn(GlobalScope.async { throw FileNotFoundException() })

    val result = mvc.perform(get("/awaitDownload/$uuid")).andReturn()

    mvc.perform(asyncDispatch(result))
        .andExpect(status().isNotFound)
  }

  @Test
  fun shouldReturn404ifFileDoesNotExist() {
    val uuid: UUID = UUID.randomUUID()
    given(storage?.downloadFile(FilePointer(uuid, "key"))).willThrow(FileNotFoundException())

    mvc.perform(get("/download/$uuid/key"))
        .andExpect(status().isNotFound)
  }

  @Test
  fun shouldReturn500ifInputstreamCantBeOpened() {
    val uuid: UUID = UUID.randomUUID()
    val resource = mock(InputStreamResource::class.java)
    given(storage?.downloadFile(FilePointer(uuid, "key"))).willReturn(resource)
    given(resource.contentLength()).willThrow(IOException())

    mvc.perform(get("/download/$uuid/key"))
        .andExpect(status().isInternalServerError)
  }
}
