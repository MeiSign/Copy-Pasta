package de.meisign.copypasta.controller

import de.meisign.copypasta.storage.FilePointer
import de.meisign.copypasta.storage.FileStorage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*


@ExtendWith(SpringExtension::class)
@WebMvcTest(UploadController::class)
class UploadControllerTest {
  @Autowired
  private val mvc: MockMvc? = null

  @MockBean
  private val storage: FileStorage? = null

  @Test
  fun uploadShouldRedirectIfSuccessful() {
    val file = MockMultipartFile("file", "orig", null, "bar".toByteArray())
    given(storage?.storeFile(file)).willReturn(FilePointer(UUID.fromString("0069a086-02c4-4cbc-a6d8-53730d110486"), "key"))

    mvc?.perform(MockMvcRequestBuilders.multipart("/upload").file(file))
        ?.andExpect(status().`is`(200))
        ?.andExpect(content().json(
            "{\"uuid\": \"0069a086-02c4-4cbc-a6d8-53730d110486\", \"key\": \"key\"}")
        )
  }
}
