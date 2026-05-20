package avr.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping("/api")
public class HelpController {

  @GetMapping(value = "/help", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> getHelp() {
    try {
      Resource resource = new ClassPathResource("static/api-docs.html");
      String html = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
      return ResponseEntity.ok(html);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("<h1>Error</h1><p>API documentation not found</p>");
    }
  }
}
