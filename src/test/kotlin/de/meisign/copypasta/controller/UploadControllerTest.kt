package de.meisign.copypasta.controller

import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.FileStorage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*


@ExtendWith(SpringExtension::class)
@WebMvcTest(UploadController::class)
class UploadControllerTest {
  @Autowired
  private val mvc: MockMvc? = null

  @MockBean
  private val storage: FileStorage? = null

  @Test
  fun successfulUploadShouldReturnPointer() {
    val file = MockMultipartFile("file", "orig", null, "bar".toByteArray())
    val uuid: UUID = UUID.randomUUID()

    given(storage?.storeFile(file, uuid)).willReturn(FilePointer(uuid, "key"))

    mvc?.perform(MockMvcRequestBuilders.multipart("/upload?uuid={uuid}", uuid).file(file))
        ?.andExpect(status().`is`(200))
        ?.andExpect(content().string(
            "{\"uuid\":\"${uuid}\",\"key\":\"key\"}")
        )
  }

  @Test
  fun successfulUploadWithoutUuidShouldReturnPointer() {
    val file = MockMultipartFile("file", "orig", null, "bar".toByteArray())
    val uuid = UUID.randomUUID()

    given(storage?.storeFile(eq(file), any())).willReturn(FilePointer(uuid, "key"))

    mvc?.perform(MockMvcRequestBuilders.multipart("/upload", uuid).file(file))
        ?.andExpect(status().`is`(200))
        ?.andExpect(content().string(
            "{\"uuid\":\"${uuid}\",\"key\":\"key\"}")
        )
  }

  private fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
  }
  private fun <T> uninitialized(): T = null as T

  private fun <T> eq(t: T): T {
    Mockito.eq<T>(t)
    return uninitialized()
  }
}
