package de.meisign.copypasta

import de.meisign.copypasta.storage.s3.CloudConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import


@SpringBootApplication
@Import(CloudConfiguration::class)
class CopyPastaApplication

fun main(args: Array<String>) {
    runApplication<CopyPastaApplication>(*args)
}

