package fpt.swp.springmvctt.itp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/"})
    public String index() {
        return "index"; // trỏ tới templates/index.html
    }
}
