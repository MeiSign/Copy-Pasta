package de.meisign.copypasta.storage

import java.util.UUID


data class FilePointer(val uuid: UUID, val key: String) {
  val path: String = "$uuid/$key"
}
