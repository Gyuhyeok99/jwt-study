package jwt.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jwt.security.auth.dto.AuthReq;
import jwt.security.auth.dto.AuthRes;
import jwt.security.auth.dto.RegisterReq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService service;

  @PostMapping("/register")
  public ResponseEntity<AuthRes> register(@RequestBody RegisterReq req) {
    return ResponseEntity.ok(service.register(req));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthRes> authenticate(@RequestBody AuthReq req) {
    return ResponseEntity.ok(service.authenticate(req));
  }

  @PostMapping("/refresh-token")
  public void refreshToken(HttpServletRequest req, HttpServletResponse res) throws IOException {
    service.refreshToken(req, res);
  }


}
