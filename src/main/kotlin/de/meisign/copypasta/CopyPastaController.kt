package de.meisign.copypasta

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class CopyPastaController {

    @GetMapping("/")
    fun upload(model: Model): String {
        model.addAttribute("message", "Hello World")
        return "uploadForm"
    }

//    @GetMapping("/download")
//    fun download() =

//    @GetMapping("/qrcode")
//    fun qrcode() =
}
