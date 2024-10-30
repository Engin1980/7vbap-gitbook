package cz.osu.vbap.favUrls.security;

import cz.osu.vbap.favUrls.services.AppService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil extends AppService {
  @Value("${app.security.privateKey}")
  private String secretKey;
  @Value("${app.security.refreshTokenExpirationSeconds}")
  private int refreshTokenExpirationInSeconds;
  @Value("${app.security.accessTokenExpirationSeconds}")
  private int accessTokenExpirationInSeconds;
  private static final String APP_USER_ID_CLAIM_NAME = "appUserId";


  public String generateAccessToken(String refreshToken) {
    String email = extractUsername(refreshToken);
    int appUserId = getAppUserId(refreshToken);
    String ret = generateToken(email, appUserId, accessTokenExpirationInSeconds);
    return ret;
  }

  public String generateRefreshToken(String email, int appUserId) {
    String ret = generateToken(email, appUserId, refreshTokenExpirationInSeconds);
    return ret;
  }

  private String generateToken(String userName, int appUserId, int expirationInSeconds) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(APP_USER_ID_CLAIM_NAME, appUserId);
    String ret = Jwts.builder()
            .claims(claims)
            .subject(userName)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + 1000L * expirationInSeconds))
            .signWith(getSignKey())
            .compact();
    return ret;
  }

  private SecretKey getSignKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public boolean isValid(String token) {
    if (token == null || token.isEmpty()) return false;
    try {
      extractAllClaims(token);
      return true;
    } catch (ExpiredJwtException e) {
      return false;
    }
  }

  public int getAppUserId(String jwt) {
    Claims claims = extractAllClaims(jwt);
    return (int) claims.get(APP_USER_ID_CLAIM_NAME);
  }

  public String getSubject(String jwt) {
    return this.extractUsername(jwt);
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts
            .parser()
            .verifyWith(getSignKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
  }

  private String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }
}

