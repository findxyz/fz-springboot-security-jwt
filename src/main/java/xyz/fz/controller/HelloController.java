package xyz.fz.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
@PreAuthorize("hasAuthority('ROLE_USER')")
public class HelloController {

    @RequestMapping("/hi")
    public String hello() {
        return "hello";
    }
}
