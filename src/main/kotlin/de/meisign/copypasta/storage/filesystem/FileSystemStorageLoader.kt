package de.meisign.copypasta.storage.filesystem

import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class FileSystemStorageLoader(private val resourceLoader: FileSystemResourceLoader = FileSystemResourceLoader()) {

  fun getResource(location: Path): Resource {
    return resourceLoader.getResource(location.toString())
  }

  fun getResources(location: Path): Array<out Resource> {
    return ResourcePatternUtils
        .getResourcePatternResolver(resourceLoader)
        .getResources(location.toString())
  }
}
