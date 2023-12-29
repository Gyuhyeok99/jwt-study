package jwt.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jwt.security.domain.token.Token;
import jwt.security.token.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import static jwt.security.util.Jwt.HEADER_AUTHORIZATION;
import static jwt.security.util.Jwt.TOKEN_PREFIX;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;

  @Override
  public void logout(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) {
    final String authHeader = request.getHeader(HEADER_AUTHORIZATION);
    final String jwt;
    if (authHeader == null ||!authHeader.startsWith(TOKEN_PREFIX)) {
      return;
    }

    jwt = authHeader.substring(7);
    String username = jwtService.extractUsername(jwt);
    Token storedToken = refreshTokenRepository.findByUser_Email(username)
            .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
    if (storedToken != null) {
      storedToken.setExpired(true);
      storedToken.setRevoked(true);
      refreshTokenRepository.save(storedToken);
      SecurityContextHolder.clearContext();
    }
  }
}
