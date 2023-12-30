package jwt.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jwt.security.auth.dto.AuthReq;
import jwt.security.auth.dto.AuthRes;
import jwt.security.auth.dto.RegisterReq;
import jwt.security.util.JwtService;
import jwt.security.domain.user.User;
import jwt.security.user.UserRepository;
import jwt.security.util.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static jwt.security.util.Jwt.HEADER_AUTHORIZATION;
import static jwt.security.util.Jwt.TOKEN_PREFIX;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RedisService redisService;
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
    //Long issuedAt = jwtService.getIssuedAt(refreshToken);
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
    //key는 사용자 이메일과 토큰 발급 시간으로 구성 // 추후에 발급 시간이 아닌 기기로 구분하는 거로 수정해야함
    //redisService.setValueOps(user.getEmail() + ":" + issuedAt, refreshToken);
    redisService.setValueOps(user.getEmail(), refreshToken);
    redisService.expireValues(user.getEmail());
  }
  @Transactional
  public void revokeAllUserTokens(User user) {
    redisService.deleteValueOps(user.getEmail());
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
        AuthRes authRes = AuthRes.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authRes);
      }
    }
  }
}
