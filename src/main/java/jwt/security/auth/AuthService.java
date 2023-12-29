package jwt.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jwt.security.auth.dto.AuthReq;
import jwt.security.auth.dto.AuthRes;
import jwt.security.auth.dto.RegisterReq;
import jwt.security.config.JwtService;
import jwt.security.domain.token.Token;
import jwt.security.domain.token.TokenType;
import jwt.security.domain.user.User;
import jwt.security.token.RefreshTokenRepository;
import jwt.security.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static jwt.security.util.Jwt.HEADER_AUTHORIZATION;
import static jwt.security.util.Jwt.TOKEN_PREFIX;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  @Transactional
  public AuthRes register(RegisterReq req) {
    User user = User.builder()
            .name(req.getName())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .role(req.getRole())
            .build();
    User savedUser = userRepository.save(user);
    String accessToken = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(savedUser, refreshToken);
    return AuthRes.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
  }

  @Transactional
  public AuthRes authenticate(AuthReq request) {
    authenticationManager.authenticate
            (new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("유저 존재하지 않음"));
    String accessToken = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, refreshToken);
    return AuthRes.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }


  @Transactional

  public void saveUserToken(User user, String refreshToken) {
    var token = Token.builder()
        .user(user)
        .token(refreshToken)
        .tokenType(TokenType.BEARER)
        .expired(false)
        .revoked(false)
        .build();
    refreshTokenRepository.save(token);
  }
  @Transactional
  public void revokeAllUserTokens(User user) {
    List<Token> validUserTokens = refreshTokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    refreshTokenRepository.saveAll(validUserTokens);
  }

  @Transactional

  public void refreshToken(
          HttpServletRequest request,
          HttpServletResponse response
  ) throws IOException {
    final String authHeader = request.getHeader(HEADER_AUTHORIZATION);
    final String refreshToken;
    final String userEmail;
    if (authHeader == null ||!authHeader.startsWith(TOKEN_PREFIX)) {
      return;
    }
    refreshToken = authHeader.substring(7);
    userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      User user = userRepository.findByEmail(userEmail)
              .orElseThrow(() -> new IllegalArgumentException("유저 존재하지 않음"));
      if (jwtService.isTokenValid(refreshToken, user)) {
        String accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        AuthRes authRes = AuthRes.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authRes);
      }
    }
  }
}
