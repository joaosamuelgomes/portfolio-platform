package com.madebyjoaogomes.portfolio.security;

import java.time.Clock;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtEncoder encoder;
    private final SecurityProperties.Jwt settings;
    private final Clock clock;

    public TokenService(JwtEncoder encoder, SecurityProperties properties, Clock clock) {
        this.encoder = encoder;
        this.settings = properties.jwt();
        this.clock = clock;
    }

    public TokenResponse issue(Authentication authentication) {
        var now = clock.instant();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(ROLE_PREFIX))
                .map(authority -> authority.substring(ROLE_PREFIX.length()))
                .sorted()
                .toList();
        var claims = JwtClaimsSet.builder()
                .issuer(settings.issuer())
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(settings.ttl()))
                .claim(ROLES_CLAIM, roles)
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        var token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", settings.ttl().toSeconds(), roles.stream().map(Role::valueOf).toList());
    }
}
