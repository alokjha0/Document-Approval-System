package com.company.das.common.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    
    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String error,
            Model model) {

        if(error != null) {

            model.addAttribute(
                    "error",
                    "Invalid email or password"
            );
        }

        return "home/index";
    }
}