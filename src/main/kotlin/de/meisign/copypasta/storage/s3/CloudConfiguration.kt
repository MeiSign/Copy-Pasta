package de.meisign.copypasta.storage.s3

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import java.net.URI

@Configuration
class CloudConfiguration(
  @Value("\${aws.s3.endpoint:https://s3.eu-west-1.amazonaws.com}") val s3Endpoint: String) {

  @Bean
  @Profile("local", "test")
  fun localAwsCredentials(): AwsCredentialsProvider {
    return StaticCredentialsProvider
      .create(AwsBasicCredentials.create("foo", "bar"))
  }

  @Bean
  @Profile("!local & !test")
  fun awsCredentials(): AwsCredentialsProvider {
    return DefaultCredentialsProvider.create()
  }

  @Bean
  fun amazonS3(credentials: AwsCredentialsProvider): S3AsyncClient {
    return S3AsyncClient
      .builder()
      .endpointOverride(URI(s3Endpoint))
      .region(Region.EU_WEST_1)
      .credentialsProvider(credentials)
      .build()
  }
}
