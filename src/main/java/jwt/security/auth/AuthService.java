package jwt.security.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jwt.security.auth.dto.*;
import jwt.security.config.exception.handler.UserHandler;
import jwt.security.kakao.KakaoService;
import jwt.security.kakao.dto.KakaoUserInfo;
import jwt.security.user.UserService;
import jwt.security.utils.JwtService;
import jwt.security.domain.user.User;
import jwt.security.user.UserRepository;
import jwt.security.utils.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static jwt.security.config.code.status.ErrorStatus.USER_NOT_FOUND;
import static jwt.security.utils.Jwt.HEADER_AUTHORIZATION;
import static jwt.security.utils.Jwt.TOKEN_PREFIX;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RedisService redisService;
  private final AuthenticationManager authenticationManager;
  private final KakaoService kakaoService;

  @Transactional
  public AuthRes register(RegisterReq req) {
    User user = User.builder()
            .name(req.getName())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .role(req.getRole())
            .build();
    User savedUser = userService.save(user);
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
    User user = userService.findByEmail(request.getEmail());
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
  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String authHeader = request.getHeader(HEADER_AUTHORIZATION);
    final String refreshToken;
    final String userEmail;
    if (authHeader == null ||!authHeader.startsWith(TOKEN_PREFIX))
      return;

    refreshToken = authHeader.substring(7);
    userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      User user = userService.findByEmail(userEmail);
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

  @Transactional
  public ResponseEntity<?> processKakaoUser(String authorizationCode) {
    // 카카오 서비스를 통해 액세스 토큰 획득
    // 액세스 토큰을 사용하여 카카오로부터 사용자 정보 획득
    KakaoUserInfo kakaoUserInfo = kakaoService.getUserInfo(kakaoService.getAccessToken(authorizationCode));
    // 사용자 이메일을 기반으로 데이터베이스에서 사용자 조회
    Optional<User> user = userService.findKakaoByEmail(kakaoUserInfo.getEmail());

    if (user.isPresent()) {
      User findUser = user.get();
      String accessToken = jwtService.generateToken(findUser);
      String refreshToken = jwtService.generateRefreshToken(findUser);
      revokeAllUserTokens(findUser);
      saveUserToken(findUser, refreshToken);
      AuthRes authRes = new AuthRes(accessToken, refreshToken);
      return ResponseEntity.ok(authRes);
    } else {
      SocialRes socialRes = new SocialRes(kakaoUserInfo.getNickname(), kakaoUserInfo.getEmail());
      return ResponseEntity.ok(socialRes);
    }
  }
}
