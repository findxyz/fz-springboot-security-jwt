package xyz.fz.config.security.filter;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ManagementAuthenticationToken extends AbstractAuthenticationToken {

    private String username;

    ManagementAuthenticationToken(String username, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.username = username;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }
}
