package de.meisign.copypasta.storage

import org.springframework.http.MediaType
import java.io.InputStream

interface FilePointer {
  fun stream(): InputStream?
  fun length(): Long
}
