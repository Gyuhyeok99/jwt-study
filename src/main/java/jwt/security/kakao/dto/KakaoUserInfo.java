package jwt.security.kakao.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfo {

    String id;
    String nickname;
    String email;

    @Builder

    public KakaoUserInfo(String id, String nickname, String email) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
    }
}


