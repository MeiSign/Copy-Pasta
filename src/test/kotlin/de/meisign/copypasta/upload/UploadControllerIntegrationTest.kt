package de.meisign.copypasta.upload

import cloud.localstack.docker.LocalstackDockerExtension
import cloud.localstack.docker.annotation.LocalstackDockerProperties
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import de.meisign.copypasta.CopyPastaApplication
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import java.util.*


@ActiveProfiles("test")
@ExtendWith(SpringExtension::class, LocalstackDockerExtension::class)
@SpringBootTest(
  webEnvironment =  SpringBootTest.WebEnvironment.MOCK,
  classes = [CopyPastaApplication::class])
@AutoConfigureMockMvc
@LocalstackDockerProperties(services = ["s3"], pullNewImage = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UploadControllerIntegrationTest(@Autowired private val mvc: MockMvc,
                                               @Autowired private val amazonS3: S3AsyncClient,
                                               @Value("\${aws.s3.bucketName}") private val bucketName: String) {

  val fileUuid: UUID = UUID.randomUUID()
  private final val fileContent = "fileContent"
  private final val fileName = "fileName"
  val file = MockMultipartFile("file", fileName, null, fileContent.toByteArray())
  val wireMockServer: WireMockServer = WireMockServer(wireMockConfig().port(8089))

  @BeforeAll
  fun beforeAll() {
    amazonS3
      .createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
      .get()

    wireMockServer.start()
  }

  @BeforeEach
  fun beforeEach() {
    wireMockServer.resetAll()
  }

  @AfterAll
  fun afterAll() {
    wireMockServer.stop()
  }

  @Test
  fun invalidRecaptchaResponseShouldReturn403() {
    wireMockServer.stubFor(get(urlPathEqualTo("/recaptcha"))
      .willReturn(
        okJson("""{"success": false, "score":0.0, "error-codes":["timeout-or-duplicate"]}""")
      )
    )

    val request = multipart("/upload?uuid={uuid}", fileUuid)
      .file(file)
      .param("recaptchaToken", "Token123")


    mvc
      .perform(request)
      .andExpect(request().asyncStarted())
      .andDo { result ->
        mvc
          .perform(asyncDispatch(result))
          .andExpect(status().isForbidden)
      }
  }

  @Test
  fun successfulUploadWithUuidShouldReturnPointer() {
    givenValidRecaptchaResponse()

    val request = multipart("/upload?uuid={uuid}", fileUuid)
      .file(file)
      .param("recaptchaToken", "Token123")

    mvc
      .perform(request)
      .andExpect(request().asyncStarted())
      .andDo { result ->
        mvc
          .perform(asyncDispatch(result))
          .andExpect(status().isOk)
          .andExpect(content().json("{'uuid':'$fileUuid','key':'$fileName'}")
      )
    }
  }

  @Test
  fun successfulUploadWithoutUuidShouldReturnPointer() {
    givenValidRecaptchaResponse()

    val request = multipart("/upload")
      .file(file)
      .param("recaptchaToken", "Token123")

    mvc
      .perform(request)
      .andExpect(request().asyncStarted())
      .andDo { result ->
        mvc.perform(asyncDispatch(result))
          .andExpect(status().isOk)
          .andExpect(content().json("{'key':'$fileName'}"))
      }
  }

  @Test
  fun uploadWithoutFileShouldReturnBadRequest() {
    val request = multipart("/upload")
      .param("recaptchaToken", "Token123")

    mvc
      .perform(request)
      .andExpect(status().isBadRequest)
  }

  @Test
  fun uploadWithoutRecaptchaTokenShouldReturnBadRequest() {
    val request = multipart("/upload")
      .file(file)

    mvc
      .perform(request)
      .andExpect(status().isBadRequest)
  }

  private fun givenValidRecaptchaResponse() {
    wireMockServer.stubFor(
      get(urlPathEqualTo("/recaptcha"))
        .willReturn(
          okJson("""{"success": true, "score":1.0}""")
        )
    )
  }
}
