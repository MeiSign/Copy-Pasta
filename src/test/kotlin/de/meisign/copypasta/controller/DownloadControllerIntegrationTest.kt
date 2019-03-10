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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*


@TestPropertySource(properties = [
  "aws.s3.pollingRetries = 3",
  "aws.s3.pollingIntervalMs = 50"
])
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class, LocalstackDockerExtension::class)
@SpringBootTest(
    webEnvironment =  SpringBootTest.WebEnvironment.MOCK,
    classes = [CopyPastaApplication::class])
@AutoConfigureMockMvc
@LocalstackDockerProperties(services = ["s3"], pullNewImage = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DownloadControllerIntegrationTest(@Autowired private val mvc: MockMvc,
                                    @Autowired private val amazonS3: S3AsyncClient,
                                    @Value("\${aws.s3.bucketName}") private val bucketName: String) {

  val fileUuid: UUID = UUID.randomUUID()
  val fileContent = "fileContent"
  val fileName = "fileName"

  @BeforeAll
  fun beforeAll() {
    amazonS3
        .createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
        .get()

    amazonS3.putObject(
        PutObjectRequest.builder().bucket(bucketName).key("$fileUuid/$fileName").build(),
        AsyncRequestBody.fromBytes(fileContent.toByteArray())
    )
  }

  @Test
  fun downloadShouldReturnFileIfFileExists() {
    mvc
      .perform(get("/download/$fileUuid/$fileName"))
      .andDo { result ->
        mvc.perform(asyncDispatch(result))
            .andExpect(status().isOk)
            .andExpect(content().bytes(fileContent.toByteArray()))
      }
  }

  @Test
  fun downloadShouldReturnNotFoundIfFileDoesNotExists() {
    mvc
      .perform(get("/download/$fileUuid/unknownFile"))
      .andDo {result ->
        mvc.perform(asyncDispatch(result))
          .andExpect(status().isNotFound)
      }
  }

  @Test
  fun downloadShouldReturnBadRequestIfUuidIsInvalid() {
    mvc
      .perform(get("/download/invalidUUID/unknownFile"))
      .andExpect(status().isBadRequest)
  }

  @Test
  fun awaitDownloadShouldReturnFilepointerIfFileExists() {
    val result = mvc
        .perform(get("/awaitDownload/$fileUuid"))
        .andReturn()

    mvc.perform(asyncDispatch(result))
        .andExpect(status().isOk)
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(content().json("{'uuid':'$fileUuid','key':'$fileName'}"))
  }

  @Test
  fun awaitDownloadShouldReturnNotFoundIfFileDoesNotExists() {
    val result = mvc
        .perform(get("/awaitDownload/${UUID.randomUUID()}"))
        .andReturn()

    mvc.perform(asyncDispatch(result))
        .andExpect(status().isNotFound)
  }

  @Test
  fun awaitDownloadShouldReturnBadRequestIfUuidIsInvalid() {
    mvc
      .perform(get("/awaitDownload/invalidUUID"))
      .andExpect(status().isBadRequest)
  }

  @Test
  fun awaitDownloadShouldReturnFilePointerIfFileIsUploaded() {
    val uuid = UUID.randomUUID()

    val result = mvc
        .perform(get("/awaitDownload/$uuid"))
    	  .andExpect(request().asyncStarted())
        .andReturn()

    amazonS3.putObject(
        PutObjectRequest.builder().bucket(bucketName).key("$uuid/newFile").build(),
        AsyncRequestBody.fromBytes("newFileContent".toByteArray())
    )

    mvc.perform(asyncDispatch(result))
      .andExpect(status().isOk)
      .andExpect(content().json("{'uuid':'$uuid','key':'newFile'}"))
  }
}
