package de.meisign.copypasta.controller

import de.meisign.copypasta.storage.FileStorage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/download")
class DownloadController(@Autowired val storage: FileStorage) {

  @RequestMapping(value = "/{uuid}")
  fun download(@PathVariable uuid: UUID): Resource {

    return storage.findFile(uuid)
        ?.stream()
        ?.let { InputStreamResource(it) }
        ?: throw NotFoundException()
  }
}

