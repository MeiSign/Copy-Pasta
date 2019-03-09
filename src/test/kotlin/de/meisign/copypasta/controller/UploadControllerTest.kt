package de.meisign.copypasta.controller

import cloud.localstack.docker.LocalstackDockerExtension
import cloud.localstack.docker.annotation.LocalstackDockerProperties
import de.meisign.copypasta.CopyPastaApplication
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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


@ActiveProfiles(value = ["local", "test"])
@ExtendWith(SpringExtension::class, LocalstackDockerExtension::class)
@SpringBootTest(
  webEnvironment =  SpringBootTest.WebEnvironment.MOCK,
  classes = [CopyPastaApplication::class])
@AutoConfigureMockMvc
@LocalstackDockerProperties(services = ["s3"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UploadControllerTest(@Autowired private val mvc: MockMvc,
                           @Autowired private val amazonS3: S3AsyncClient,
                           @Value("\${aws.s3.bucketName}") private val bucketName: String) {

  val fileUuid: UUID = UUID.randomUUID()
  val fileContent = "fileContent"
  val fileName = "fileName"
  val file = MockMultipartFile("file", fileName, null, fileContent.toByteArray())

  @BeforeAll
  fun beforeAll() {
    amazonS3
      .createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
      .get()
  }

  @Test
  fun successfulUploadWithUuidShouldReturnPointer() {
    mvc
      .perform(multipart("/upload?uuid={uuid}", fileUuid).file(file))
      .andExpect(request().asyncStarted())
      .andDo { result ->
        mvc
          .perform(asyncDispatch(result))
          .andExpect(status().isOk)
          .andExpect(content().json("{'uuid':'${fileUuid}','key':'$fileName'}")
      )
    }
  }

  @Test
  fun successfulUploadWithoutUuidShouldReturnPointer() {
    mvc
      .perform(multipart("/upload", fileUuid).file(file))
      .andExpect(request().asyncStarted())
      .andDo { result ->
        mvc.perform(asyncDispatch(result))
          .andExpect(status().isOk)
          .andExpect(content().json("{'key':'$fileName'}"))
      }
  }

  @Test
  fun uploadWithoutFileShouldReturnBadRequest() {
    mvc
      .perform(multipart("/upload", fileUuid))
      .andExpect(status().isBadRequest)
  }
}
