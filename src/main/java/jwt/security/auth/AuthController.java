package jwt.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jwt.security.auth.dto.AuthReq;
import jwt.security.auth.dto.AuthRes;
import jwt.security.auth.dto.RegisterReq;
import jwt.security.kakao.KakaoService;
import jwt.security.kakao.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthService authService;
  private final KakaoService kakaoService;

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


  @GetMapping(path="/kakaoLogin")
  public ResponseEntity<String> login(@RequestParam("code") String code) {
    //log.info(AUTH_CODE_MSG2 + code);
    String accessToken = kakaoService.getAccessToken(code);
    KakaoUserInfo kakaoUserInfo = null;
    if (accessToken != null && !accessToken.isEmpty()) {
      kakaoUserInfo = kakaoService.getUserInfo(accessToken);
      if (kakaoUserInfo != null) {
        log.info("kakaoUserInfo : " + kakaoUserInfo.toString());
        return ResponseEntity.ok("LOGIN_MSG + AUTH_CODE_MSG + code" + " )");
      }
    }
    return ResponseEntity.badRequest().body("LOGIN_FAIL_MSG");
  }

}
