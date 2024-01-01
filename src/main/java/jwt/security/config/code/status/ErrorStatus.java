package jwt.security.config.code.status;

import jwt.security.config.code.BaseErrorCode;
import jwt.security.config.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","로그인 인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 멤버 관려 에러
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "사용자가 없습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "MEMBER4002", "비밀번호가 일치하지 않습니다."),
    PASSWORD_NOT_MATCH_CONFIRM(HttpStatus.BAD_REQUEST, "MEMBER4003", "새비밀번호와 재입력한 새비밀번호가 일치하지 않습니다."),
    USER_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "MEMBER4004", "이미 존재하는 사용자입니다."),

    // 이메일 관련 에러
    EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "EMAIL4001", "이메일이 존재하지 않습니다."),
    EMAIL_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "EMAIL4002", "이미 존재하는 이메일입니다."),
    EMAIL_SEND_FAIL(HttpStatus.BAD_REQUEST, "EMAIL4003", "이메일 전송에 실패했습니다."),
    EMAIL_AUTH_FAIL(HttpStatus.BAD_REQUEST, "EMAIL4004", "이메일 인증에 실패했습니다."),
    EMAIL_AUTH_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL4005", "이메일 인증 시간이 만료되었습니다."),
    EMAIL_AUTH_ALREADY(HttpStatus.BAD_REQUEST, "EMAIL4006", "이미 인증된 이메일입니다."),
    EMAIL_AUTH_NOT_FOUND(HttpStatus.BAD_REQUEST, "EMAIL4007", "이메일 인증을 먼저 진행해주세요."),
    EMAIL_AUTH_NOT_MATCH(HttpStatus.BAD_REQUEST, "EMAIL4008", "이메일 인증번호가 일치하지 않습니다."),

    // 토근 관련 에러
    TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "TOKEN4001", "토큰이 존재하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "TOKEN4002", "토큰이 만료되었습니다."),
    KAKAO_TOKEN_RECEIVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN4003", "카카오 서버로부터 액세스 토큰을 받는데 실패했습니다."),
    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
