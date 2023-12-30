package jwt.security.user;

import jwt.security.config.ApiResponse;
import jwt.security.user.dto.ChangePasswordReq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping
    public ApiResponse<?> changePassword(@RequestBody ChangePasswordReq request, Principal connectedUser) {
        userService.changePassword(request, connectedUser);
        return ApiResponse.onSuccess("비밀번호 변경 완료");
    }
}
