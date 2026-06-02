package com.csproj.Cyberlab.API.jwt;

import com.csproj.Cyberlab.API.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

//---------------------------------------------------------------
// Provides JWT business logic for access and refresh tokens.
//---------------------------------------------------------------
@Service
public class JwtService {
    private static final String SECRET_KEY = Base64.getEncoder()
            .encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());

    /**
     * Builds and signs a JWT with the supplied type and claims
     *
     * @param tokenType Type of JWT (ACCESS | REFRESH)
     * @param ud UserDetails used to set JWT sub if generating an ACCESS token
     * @return Token
     */
    public String generate(TokenType tokenType, UserDetails ud) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + tokenType.getExpirationMillis());

        JwtBuilder builder = Jwts.builder()
                .claim("type", tokenType.name())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSignKey(), SignatureAlgorithm.HS256);

        if (tokenType.equals(TokenType.ACCESS)) {
            builder.setSubject(ud.getUsername());
        }

        return builder.compact();
    }

    /**
     * Determines if an existing JWT is valid
     * Ensures the token is non-expired
     * Checks if the requesting User matches the JWTs issued subject when type is ACCESS
     *
     * @param token JWT refresh tokens must be hashed before invocation
     * @param ud UserDetails from requesting User
     * @return True if token is valid
     */
    public boolean isValid(String token, UserDetails ud) {
        if (isExpired(token)) return false;

        TokenType tt = extractTokenType(token);

        if (tt.equals(TokenType.ACCESS)) {
            return extractUsername(token).equals(ud.getUsername());
        }
        else if (tt.equals(TokenType.REFRESH)) {
            return ((User) ud).getRefreshToken().equals(token);
        }

        throw new IllegalStateException("Unhandled TokenType in JWT validation");
    }

    /**
     * Checks if JWT is expired by comparing with the current time
     *
     * @param token JWT
     * @return True if expired
     */
    public boolean isExpired(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return exp.before(new Date());
    }

    /**
     * Extract the token type from a JWT claims
     *
     * @param token JWT
     * @return TokenType
     */
    public TokenType extractTokenType(String token) {
        return TokenType.valueOf(extractClaim(token, claims -> claims.get("type", String.class)));
    }

    /**
     * Extract the username (subject) from a JWT
     *
     * @param token JWT
     * @return String username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a claim by passing the claim's name through claimsResolver
     *
     * @param token JWT
     * @param claimsResolver Resolver for Claim type
     * @return Claim contents
     * @param <T> Claim datatype
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims c = extractClaims(token);
        return claimsResolver.apply(c);
    }

    /**
     * Extracts all claims from a JWT
     *
     * @param token JWT
     * @return Claims
     */
    private Claims extractClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Get key to sign JWT with
     *
     * @return Key
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
