package com.ouzacocktailbarkitchen.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.ouzacocktailbarkitchen.controller.SpaController;

@Controller
public class SpaController {

    @GetMapping(value = {"/", "/{path:[^\\.]*}"})
    public String fallback() {
        return "forward:/index.html";
    }
}