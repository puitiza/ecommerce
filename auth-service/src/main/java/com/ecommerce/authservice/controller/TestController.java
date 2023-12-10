package com.ecommerce.authservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/ok", produces = MediaType.APPLICATION_JSON_VALUE)
    public String registerUser() {
        return "ok";
    }
}
