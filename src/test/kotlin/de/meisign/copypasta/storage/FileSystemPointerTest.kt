package de.meisign.copypasta.storage

import org.junit.Test

import org.junit.Assert.*
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

class FileSystemPointerTest {

  @Test
  fun streamOfNonExistentFileShouldBeNull() {
    assertNull(FileSystemPointer(File("nonExistentFile")).stream())
  }

  @Test
  fun streamShouldBeInputStreamOfFile() {
    val fileData = "test123"
    val path = Files.write(Paths.get("test.txt"), fileData.toByteArray())
    val stream: InputStream? = FileSystemPointer(path.toFile()).stream()
    assertArrayEquals(stream?.readBytes(), fileData.toByteArray())
    Files.delete(path)
    stream?.let { it.close() }
  }
}
