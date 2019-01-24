package xyz.fz.config;

import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import xyz.fz.config.security.filter.JwtAuthenticationFilter;
import xyz.fz.config.security.filter.ManagementAuthenticationFilter;
import xyz.fz.config.security.filter.SecurityFilterPath;

import javax.annotation.Resource;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Resource
    private SecurityFilterPath securityFilterPath;

    @Resource
    private ManagementAuthenticationFilter managementAuthenticationFilter;

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers(securityFilterPath.getNoAuthPaths()).permitAll();

        managementAuthenticationFilter.configure(http);

        jwtAuthenticationFilter.configure(http);

        http.httpBasic().disable();
        http.formLogin().disable();
        http.logout().disable();
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
