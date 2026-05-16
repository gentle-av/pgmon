package avr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "forward:/static/index.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/static/index.html";
    }
}
