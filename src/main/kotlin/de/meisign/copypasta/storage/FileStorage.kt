package de.meisign.copypasta.storage

import java.util.*

interface FileStorage {
  fun findFile(uuid: UUID): FilePointer?
}
