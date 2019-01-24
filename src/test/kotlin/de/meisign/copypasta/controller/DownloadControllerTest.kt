package de.meisign.copypasta.controller

import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.FileStorage
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*


@RunWith(SpringRunner::class)
@WebMvcTest(DownloadController::class)
class DownloadControllerTest {

  @Autowired
  private val mvc: MockMvc? = null

  @MockBean
  private val storage: FileStorage? = null

  @Mock
  private val filePointer: FilePointer? = null

  @Test
  fun controllerShouldReturn200ForCorrectUuid() {
    val uuid: UUID = UUID.randomUUID()
    val bytes = "Hello World".toByteArray()
    val stream = "Hello World".byteInputStream()

    given(storage?.findFile(uuid)).willReturn(filePointer)
    given(filePointer?.stream()).willReturn(stream)

    mvc?.perform(get("/download/" + uuid.toString()))
        ?.andExpect(status().isOk)
        ?.andExpect(content().bytes(bytes))
  }

  @Test
  fun controllerShouldReturn404ForWrongUuid() {
    val uuid: UUID = UUID.randomUUID()

    given(storage?.findFile(uuid)).willReturn(null)

    mvc?.perform(get("/download/" + uuid.toString()))
        ?.andExpect(status().isNotFound)
  }
}
