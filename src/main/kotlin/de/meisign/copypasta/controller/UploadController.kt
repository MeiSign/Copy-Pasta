package de.meisign.copypasta.controller

import de.meisign.copypasta.storage.FileStorage
import de.meisign.copypasta.storage.StorageException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class UploadController(@Autowired private val storage: FileStorage) {

  @PostMapping("/upload")
  fun upload(@RequestParam("file") file: MultipartFile,
             redirectAttributes: RedirectAttributes): String {
    val uuid = storage.storeFile(file)?.let {
      redirectAttributes.addFlashAttribute("uuid", it.toString())
      return "redirect:/qrcode"
    }?: throw StorageException()
  }

  @GetMapping("/upload")
  fun uploadForm(model: Model): String {
    return "uploadForm"
  }

  @GetMapping("/qrcode")
  fun qrcode(): String {
    return "qrcode"
  }
}
