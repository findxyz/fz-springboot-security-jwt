package xyz.fz.config.security.filter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Component
public class ManagementAuthenticationFilter extends OncePerRequestFilter implements InitializingBean, HttpSecurityConfigure {

    private final List<RequestMatcher> managementPaths = new ArrayList<>();

    @Resource
    private SecurityFilterPath securityFilterPath;

    @Value("${spring.security.user.name}")
    private String username;

    @Value("${spring.security.user.password}")
    private String password;

    @Value("${spring.security.user.roles}")
    private String roles;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        if (matches(httpServletRequest)) {
            String header = httpServletRequest.getHeader("Authorization");

            if (header == null || !header.toLowerCase().startsWith("basic ")) {
                unauthorized(httpServletResponse);
                return;
            }

            String[] basic = header.split(" ");
            String[] namePass = new String(Base64.getDecoder().decode(basic[1])).split(":");
            if (username.equals(namePass[0]) && password.equals(namePass[1])) {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roles);
                ManagementAuthenticationToken managementAuthenticationToken = new ManagementAuthenticationToken(username, Collections.singleton(authority));
                SecurityContextHolder.getContext().setAuthentication(managementAuthenticationToken);
            } else {
                unauthorized(httpServletResponse);
                return;
            }
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    @Override
    public void afterPropertiesSet() {
        for (String managementPath : securityFilterPath.getManagementPaths()) {
            managementPaths.add(new AntPathRequestMatcher(managementPath));
        }
    }

    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers(securityFilterPath.getManagementPaths()).hasRole(roles);
        http.addFilterAfter(this, HeaderWriterFilter.class);
    }

    private boolean matches(HttpServletRequest request) {
        for (RequestMatcher matcher : managementPaths) {
            if (matcher.matches(request)) {
                return true;
            }
        }
        return false;
    }

    private void unauthorized(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.addHeader("WWW-Authenticate", "Basic realm=\"Realm\"");
        httpServletResponse.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }
}
