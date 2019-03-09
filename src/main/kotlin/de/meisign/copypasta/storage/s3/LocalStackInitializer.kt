package de.meisign.copypasta.storage.s3

import cloud.localstack.Localstack
import cloud.localstack.docker.LocalstackDocker
import cloud.localstack.docker.annotation.LocalstackDockerConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.CreateBucketRequest

@Component
@Profile("local")
class LocalStackInitializer(
  @Autowired val amazonS3: S3AsyncClient,
  @Value("\${aws.s3.bucketName}") private val bucketName: String) {

  private val localstackDocker: LocalstackDocker = LocalstackDocker.INSTANCE

  @EventListener(ContextRefreshedEvent::class)
  fun onApplicationEvent(event: ContextRefreshedEvent) {
    Localstack.teardownInfrastructure()
    val dockerConfig = LocalstackDockerConfiguration
      .builder()
      .environmentVariables(mapOf("SERVICES" to "s3"))
      .build()

    this.localstackDocker.startup(dockerConfig)
    amazonS3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build()).get()
  }

  @EventListener(ContextClosedEvent::class)
  fun onApplicationEvent(event: ContextClosedEvent) {
    this.localstackDocker.stop()
  }
}
