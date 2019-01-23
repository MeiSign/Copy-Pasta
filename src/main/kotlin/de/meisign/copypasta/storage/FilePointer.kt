package de.meisign.copypasta.storage

import java.io.InputStream

interface FilePointer {
  fun stream(): InputStream?
}
