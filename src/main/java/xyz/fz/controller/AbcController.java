package xyz.fz.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/abc")
@PreAuthorize("hasRole('ROLE_USER')")
public class AbcController {

    @RequestMapping("/")
    public String abc() {
        return "abc";
    }
}
