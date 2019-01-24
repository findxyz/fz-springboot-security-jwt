package xyz.fz.config.security.filter;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private String jwt;

    JwtAuthenticationToken(String jwt, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.jwt = jwt;
        setAuthenticated(true);
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return jwt;
    }
}
