package jwt.security.user;

import jwt.security.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional

    public void changePassword(ChangePasswordReq req, Principal connectedUser) {

        User user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        log.info("현재 비밀번호 : {}", user.getPassword());
        // 만약 현재 비밀번호가 맞지 않다면 예외
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
        // 만약 새로운 비밀번호와 확인 비밀번호가 일치하지 않다면 예외
        if (!req.getNewPassword().equals(req.getConfirmationPassword())) {
            throw new IllegalStateException("새 비밀번호가 일치하지 않습니다.");
        }
        // 비밀번호 업데이트
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        log.info("비밀번호 변경 완료 : {}", user.getPassword());
        // 새 비밀번호 저장인데 가독성을 위해 작성함. 작성하지 않아도 무방
        userRepository.save(user);
    }
}
