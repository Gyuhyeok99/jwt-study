package jwt.security.config.exception.handler;


import jwt.security.config.code.BaseErrorCode;
import jwt.security.config.exception.GeneralException;

public class UserHandler extends GeneralException {
    public UserHandler(BaseErrorCode code) {
        super(code);
    }
}
