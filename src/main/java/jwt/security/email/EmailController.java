package jwt.security.email;

import jakarta.validation.Valid;
import jwt.security.config.ApiResponse;
import jwt.security.email.dto.EmailCheckDto;
import jwt.security.email.dto.EmailRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(("/api/v1/auth"))
public class EmailController {
    private final MailSendService mailService;
    @PostMapping ("/mailSend")
    public ApiResponse<String> mailSend(@RequestBody @Valid EmailRequestDto emailDto){
        log.info("이메일 인증 요청");
        log.info("이메일 인증 이메일 : {}", emailDto.getEmail());
        return ApiResponse.onSuccess(mailService.joinEmail(emailDto.getEmail()));
    }
    @PostMapping("/mailauthCheck")
    public ApiResponse<Boolean> AuthCheck(@RequestBody @Valid EmailCheckDto emailCheckDto) {
        return ApiResponse.onSuccess(mailService.checkAuthNum(emailCheckDto.getEmail(), emailCheckDto.getAuthNum()));
    }
}