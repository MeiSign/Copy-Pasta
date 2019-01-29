package de.meisign.copypasta.controller

import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.FileStorage
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders.CONTENT_LENGTH
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*


@RunWith(SpringRunner::class)
@WebMvcTest(DownloadController::class)
class DownloadControllerTest {

  @Autowired
  private val mvc: MockMvc? = null

  @MockBean
  private val storage: FileStorage? = null

  @Test
  fun controllerShouldReturn200ForCorrectUuid() {
    val uuid: UUID = UUID.randomUUID()
    val bytes = "Hello World".toByteArray()

    given(storage?.downloadFile(FilePointer(uuid, "key"))).willReturn(bytes)

    mvc?.perform(get("/download/$uuid/key"))
        ?.andExpect(status().isOk)
        ?.andExpect(content().bytes(bytes))
        ?.andExpect(header().longValue(CONTENT_LENGTH, bytes.size.toLong()))
  }
}
