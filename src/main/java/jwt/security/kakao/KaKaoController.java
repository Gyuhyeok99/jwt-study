package jwt.security.kakao;

import jwt.security.config.ApiResponse;
import jwt.security.kakao.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/kakao")
@RequiredArgsConstructor
@Slf4j
public class KaKaoController {

    private final KakaoService kakaoService;

    @GetMapping("/login")
    public ApiResponse<KakaoUserInfo> login(@RequestParam("code") String code) {
        log.info("인증 부여 코드 : {}", code);
        String accessToken = kakaoService.getAccessToken(code);
        log.info("accessToken : {}", accessToken);
        return ApiResponse.onSuccess(kakaoService.getUserInfo(accessToken));
    }
}
