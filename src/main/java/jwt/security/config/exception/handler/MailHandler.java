package jwt.security.config.exception.handler;

import jwt.security.config.code.BaseErrorCode;
import jwt.security.config.exception.GeneralException;

public class MailHandler extends GeneralException {
    public MailHandler(BaseErrorCode code) {
        super(code);
    }
}
