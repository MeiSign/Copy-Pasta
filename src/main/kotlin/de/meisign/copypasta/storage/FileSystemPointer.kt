package de.meisign.copypasta.storage

import java.io.*

class FileSystemPointer(val file: File) : FilePointer {
  override fun stream(): InputStream? {
    return try {
      BufferedInputStream(FileInputStream(file))
    } catch (e: FileNotFoundException) {
      null
    }
  }
}
