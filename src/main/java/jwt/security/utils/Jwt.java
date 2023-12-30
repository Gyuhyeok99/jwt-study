package jwt.security.utils;

public class Jwt {

    public final static String HEADER_AUTHORIZATION = "Authorization";
    public final static String TOKEN_PREFIX = "Bearer ";

    public final static String EMAIL_TITLE = "회원 가입 인증 이메일 입니다.";

    public final static String EMAIL_CONTENT_PREFIX = "JWT TEST용 프로젝트 로그인 인증입니다." +
            "<br><br>" +
            "인증 번호는 <strong><i>&lt;여기에인증번호&gt;</i></strong> 입니다." +
            "<br>" +
            "인증번호를 제대로 입력해주세요";

    public final static String EMAIL_CONTENT_SUFFIX = "";
}
