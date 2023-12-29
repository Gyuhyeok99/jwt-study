package jwt.security.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

  @Value("${jwt.secret_key}")
  private String secretKey;
  @Value("${jwt.expiration}")
  private Long accessExpiration;
  @Value("${jwt.refresh-token.expiration}")
  private Long refreshExpiration;
  @Value("${jwt.issuer}")
  private String issuer;

  //JWT 토큰에서 subject를 추출하여 사용자 이름을 반환
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  //토큰에서 특정 클레임을 추출
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  //사용자 정보를 바탕으로 JWT 토큰을 생성
  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public String generateToken(
      Map<String, Object> extraClaims,
      UserDetails userDetails
  ) {
    return buildToken(extraClaims, userDetails, accessExpiration);
  }

  //사용자 정보를 바탕으로 리프레시 토큰을 생성
  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, refreshExpiration);
  }

  // 주어진 클레임, 사용자 정보, 그리고 만료 시간을 바탕으로 JWT 토큰을 생성
  private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return Jwts
            .builder()
            .setClaims(extraClaims)
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setIssuer(issuer)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
  }


  //토큰이 유효한지 확인
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  //토큰이 만료되었는지 확인
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  //토큰에서 만료 시간을 추출
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  //토큰에서 모든 클레임을 추출
  private Claims extractAllClaims(String token) {
    return Jwts
        .parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  //서명 키 반환, 토큰 생성하고 검증할 때 사용
  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
