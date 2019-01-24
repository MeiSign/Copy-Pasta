package de.meisign.copypasta.storage

import java.io.*
import java.nio.file.Files

class FileSystemPointer(val file: File) : FilePointer {

  override fun stream(): InputStream? {
    return try {
      BufferedInputStream(FileInputStream(file))
    } catch (e: FileNotFoundException) {
      null
    }
  }

  override fun length(): Long = file.length()
}
