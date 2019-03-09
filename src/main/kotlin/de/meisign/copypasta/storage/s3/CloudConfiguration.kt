package de.meisign.copypasta.storage.s3

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.aws.context.config.annotation.EnableContextInstanceData
import org.springframework.cloud.aws.context.config.annotation.EnableContextResourceLoader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("!local")
@Configuration
@EnableContextInstanceData
@EnableContextResourceLoader
class CloudConfiguration {
  @Value("\${s3.endpoint:https://s3.eu-west-1.amazonaws.com}")
  val s3Endpoint: String = ""

  @Value("\${s3.region:eu-west-1}")
  val s3Region: String = ""

  @Bean
  @Primary
  fun amazonS3Client(): AmazonS3 {
    val endpointConfig = AwsClientBuilder.EndpointConfiguration(
        s3Endpoint, s3Region)

    return AmazonS3ClientBuilder
        .standard()
        .withEndpointConfiguration(endpointConfig)
        .build()
  }
}
