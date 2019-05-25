package de.meisign.copypasta.recaptcha

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RecaptchaConfiguration {

  @Bean
  fun recaptchaRestTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
    return restTemplateBuilder.build()
  }
}
