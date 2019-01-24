package xyz.fz.config.security.filter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class SecurityFilterPath implements InitializingBean {
    private String[] authPaths = new String[]{"/**"};

    private String[] noAuthPaths = new String[]{"/", "/login/**", "/logout/**"};

    private String[] staticPaths = new String[]{"/pubs/**"};

    private String[] managementPaths = new String[]{
            "/metrics/**",
            "/autoconfig/**",
            "/heapdump/**",
            "/beans/**",
            "/info/**",
            "/mappings/**",
            "/health/**",
            "/configprops/**",
            "/auditevents/**",
            "/jolokia/**",
            "/loggers/**",
            "/trace/**",
            "/dump/**",
            "/env/**"
    };

    String[] getAuthPaths() {
        return authPaths;
    }

    public String[] getNoAuthPaths() {
        return noAuthPaths;
    }

    String[] getManagementPaths() {
        return managementPaths;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        noAuthPaths = arrayMerge(noAuthPaths, staticPaths, managementPaths);
    }

    private String[] arrayMerge(String[]... array) {
        int length = 0;
        for (String[] arr : array) {
            length += arr.length;
        }
        String[] newArr = new String[length];
        int dstIndex = 0;
        for (String[] arr : array) {
            System.arraycopy(arr, 0, newArr, dstIndex, arr.length);
            dstIndex += arr.length;
        }
        return newArr;
    }
}
