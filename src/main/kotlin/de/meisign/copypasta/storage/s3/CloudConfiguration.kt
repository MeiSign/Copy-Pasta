package de.meisign.copypasta.storage.s3

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportResource
import org.springframework.context.annotation.Profile

@Profile("!local")
@Configuration
@ImportResource("classpath*:cloudContext.xml")
class CloudConfiguration
