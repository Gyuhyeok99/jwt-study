package jwt.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jwt.security.auth.dto.AuthReq;
import jwt.security.auth.dto.AuthRes;
import jwt.security.auth.dto.RegisterReq;
import jwt.security.config.ApiResponse;
import jwt.security.kakao.KakaoService;
import jwt.security.kakao.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<AuthRes> register(@RequestBody RegisterReq req) {
    return ResponseEntity.ok(authService.register(req));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthRes> authenticate(@RequestBody AuthReq req) {
    return ResponseEntity.ok(authService.authenticate(req));
  }

  @PostMapping("/refresh-token")
  public void refreshToken(HttpServletRequest req, HttpServletResponse res) throws IOException {
    authService.refreshToken(req, res);
  }

  @GetMapping("/kakao-login")
  public ApiResponse<ResponseEntity<?>> login(@RequestParam("code") String code) {
    log.info("인증 부여 코드 : {}", code);
    return ApiResponse.onSuccess(authService.processKakaoUser(code));
  }
}
