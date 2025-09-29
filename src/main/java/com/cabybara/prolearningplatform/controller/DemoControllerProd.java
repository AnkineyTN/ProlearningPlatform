package com.cabybara.prolearningplatform.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Profile("prod")
public class DemoControllerProd {

    @GetMapping(value = "/hello-world")
    public String helloWorld() {
        return "Hello Product Environment!";
    }

}