package de.meisign.copypasta.storage

import org.junit.Assert.*
import org.junit.Test
import java.io.File
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

    val pointer = FileSystemPointer(path.toFile())
    assertArrayEquals(pointer.stream()?.readBytes(), fileData.toByteArray())
    assertEquals(pointer.length(), fileData.length.toLong())
    Files.delete(path)
  }
}
