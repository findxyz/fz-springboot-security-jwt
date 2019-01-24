package xyz.fz.config.security.filter;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.fz.entity.User;
import xyz.fz.service.UserService;
import xyz.fz.util.JwtUtil;
import xyz.fz.util.SessionLocal;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter implements InitializingBean, HttpSecurityConfigure {

    @Resource
    private UserService userService;

    @Value("${jwt.name}")
    private String jwtName;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private SecurityFilterPath securityFilterPath;

    private final List<RequestMatcher> authMatchers = new ArrayList<>();

    private final List<RequestMatcher> noAuthMatchers = new ArrayList<>();

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        if (matches(httpServletRequest)) {
            String jwt = httpServletRequest.getHeader(jwtName);
            if (null == jwt || "".equals(jwt)) {
                Cookie[] cookies = httpServletRequest.getCookies();
                if (cookies != null && cookies.length > 0) {
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals(jwtName)) {
                            jwt = cookie.getValue();
                        }
                    }
                }
            }
            if (null == jwt || "".equals(jwt)) {
                unauthorized(httpServletResponse, "jwt is empty");
                return;
            }
            SignedJWT signedJWT = jwtUtil.verifyJwt(jwt);
            if (signedJWT == null) {
                unauthorized(httpServletResponse, "jwt is wrong");
                return;
            }
            try {
                User user = userService.load(Long.parseLong(signedJWT.getJWTClaimsSet().getJWTID()));
                if (signedJWT.getJWTClaimsSet().getClaim("version") != null
                        && Integer.parseInt(signedJWT.getJWTClaimsSet().getClaim("version").toString()) == user.getVersion()) {
                    SessionLocal.setUser(user);
                    SecurityContextHolder.getContext().setAuthentication(
                            new JwtAuthenticationToken(jwt, Collections.singleton(new SimpleGrantedAuthority(user.getRole())))
                    );
                    httpServletResponse.addCookie(jwtUtil.jwtCookie(jwt));
                } else {
                    unauthorized(httpServletResponse, "jwt is wrong");
                    return;
                }
            } catch (ParseException e) {
                unauthorized(httpServletResponse, "jwt is wrong");
                return;
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    @Override
    public void afterPropertiesSet() {
        for (String authPath : securityFilterPath.getAuthPaths()) {
            authMatchers.add(new AntPathRequestMatcher(authPath));
        }
        for (String noAuthPath : securityFilterPath.getNoAuthPaths()) {
            noAuthMatchers.add(new AntPathRequestMatcher(noAuthPath));
        }
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers(securityFilterPath.getAuthPaths()).authenticated();
        http.addFilterAfter(this, HeaderWriterFilter.class);
    }

    private boolean matches(HttpServletRequest request) {
        for (RequestMatcher matcher : noAuthMatchers) {
            if (matcher.matches(request)) {
                return false;
            }
        }
        for (RequestMatcher matcher : authMatchers) {
            if (matcher.matches(request)) {
                return true;
            }
        }
        return false;
    }

    private void unauthorized(HttpServletResponse httpServletResponse, String msg) throws IOException {
        httpServletResponse.addCookie(jwtUtil.clearJwtCookie());
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse.setContentType("application/json; charset=utf-8");
        httpServletResponse.getWriter().write("{\"success\": false, \"message\": \"" + msg + "\", \"redirect\": \"login\"}");
    }
}
