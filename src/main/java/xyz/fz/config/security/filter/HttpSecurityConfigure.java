package xyz.fz.config.security.filter;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface HttpSecurityConfigure {
    void configure(HttpSecurity http) throws Exception;
}
