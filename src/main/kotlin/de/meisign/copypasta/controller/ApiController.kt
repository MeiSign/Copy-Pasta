package de.meisign.copypasta.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class ApiController {

  @GetMapping("/api/test")
  fun test(): String {
    return "Hello, the time at the server is now " + Date() + "\n";
  }
}
