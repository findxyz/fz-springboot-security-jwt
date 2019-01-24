package xyz.fz.util;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.servlet.http.Cookie;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil implements InitializingBean {

    @Value("${jwt.key}")
    private String jwtKeyBase64;

    private byte[] jwtKey;

    @Value("${jwt.name}")
    private String jwtName;

    @Value("${jwt.session.timeout}")
    private int jwtTimeout;

    public String createJwt(String jwtId, String subject, Date exp, int version) {
        try {
            JWSSigner signer = new MACSigner(jwtKey);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .jwtID(jwtId)
                    .subject(subject)
                    .expirationTime(exp)
                    .claim("version", version)
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception e) {
            return null;
        }
    }

    public SignedJWT verifyJwt(String jwt) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(jwt);
            JWSVerifier verifier = new MACVerifier(jwtKey);
            if (signedJWT.verify(verifier)) {
                if (signedJWT.getJWTClaimsSet().getExpirationTime().after(DateTime.now().minusSeconds(jwtTimeout).toDate())) {
                    return signedJWT;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public Cookie jwtCookie(String jwt) {
        Cookie cookie = new Cookie(jwtName, jwt);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(jwtTimeout);
        return cookie;
    }

    public Cookie clearJwtCookie() {
        Cookie cookie = new Cookie(jwtName, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        return cookie;
    }

    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] sharedSecret = new byte[32];
        random.nextBytes(sharedSecret);

        System.out.println(DigestUtils.md5DigestAsHex(sharedSecret));
        String sharedSecretBase64 = Base64.getEncoder().encodeToString(sharedSecret);
        System.out.println(sharedSecretBase64);
        System.out.println(DigestUtils.md5DigestAsHex(Base64.getDecoder().decode(sharedSecretBase64)));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        jwtKey = Base64.getDecoder().decode(jwtKeyBase64);
    }
}
