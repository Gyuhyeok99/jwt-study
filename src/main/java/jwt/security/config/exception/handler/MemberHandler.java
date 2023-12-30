package jwt.security.config.exception.handler;


import jwt.security.config.code.BaseErrorCode;
import jwt.security.config.exception.GeneralException;

public class MemberHandler extends GeneralException {
    public MemberHandler(BaseErrorCode code) {
        super(code);
    }
}
