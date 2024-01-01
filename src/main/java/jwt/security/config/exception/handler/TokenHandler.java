package jwt.security.config.exception.handler;

import jwt.security.config.code.BaseErrorCode;
import jwt.security.config.exception.GeneralException;

public class TokenHandler extends GeneralException {
    public TokenHandler(BaseErrorCode code) {
        super(code);
    }
}
