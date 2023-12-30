package jwt.security.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jwt.security.config.exception.handler.MailHandler;
import jwt.security.utils.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

import static jwt.security.config.code.status.ErrorStatus.*;
import static jwt.security.config.code.status.ErrorStatus.EMAIL_AUTH_NOT_MATCH;
import static jwt.security.utils.Jwt.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailSendService {
    private final JavaMailSender mailSender;
    private final RedisService redisService;

    private int authNumber;

    @Value("${spring.mail.username}")
    private String username;

    public Boolean checkAuthNum(String email, String authNum) {
        String storedAuthNum = redisService.getValueOps(email);
        if (storedAuthNum != null && storedAuthNum.equals(authNum)) {
            log.info("이메일 인증 성공");
            return true;
        } else {
            log.info("이메일 인증 실패");
            throw new MailHandler(EMAIL_AUTH_NOT_MATCH);
        }
    }


    //임의의 6자리 양수를 반환
    private void makeRandomNumber() {
        Random r = new Random();
        authNumber = r.ints(100000,999999)
                .findFirst()
                .getAsInt();
    }


    //mail을 어디서 보내는지, 어디로 보내는지 , 인증 번호를 html 형식으로 어떻게 보내는지 작성
    public String joinEmail(String email) {
        // 이메일에 대한 기존 인증번호가 있는지 확인하고, 있다면 삭제
        //String oldAuthNum = redisUtil.getData(email);
        String oldAuthNum = redisService.getValueOps(email);
        if (oldAuthNum != null) {
            log.info("기존 인증번호 삭제 : {}", oldAuthNum);
            //redisUtil.deleteData(email);
            redisService.deleteValueOps(email);
        }
        makeRandomNumber();
        mailSend(username, email, EMAIL_TITLE, EMAIL_CONTENT_PREFIX + authNumber + EMAIL_CONTENT_SUFFIX);
        return Integer.toString(authNumber);
    }

    //이메일을 전송합니다.
    public void mailSend(String setFrom, String toMail, String title, String content) {
        //JavaMailSender 객체를 사용하여 MimeMessage 객체를 생성
        MimeMessage message = mailSender.createMimeMessage();
        try {
            //이메일 메시지와 관련된 설정을 수행
            // true를 전달하여 multipart 형식의 메시지를 지원하고, "utf-8"을 전달하여 문자 인코딩을 설정
            MimeMessageHelper helper = new MimeMessageHelper(message,true,"utf-8");
            //이메일의 발신자 주소 설정
            helper.setFrom(setFrom);
            //이메일의 수신자 주소 설정
            helper.setTo(toMail);
            //이메일의 제목을 설정
            helper.setSubject(title);
            //이메일의 내용 설정 두 번째 매개 변수에 true를 설정하여 html 설정
            helper.setText(content,true);
            mailSender.send(message);
        } catch (MessagingException e) {
            //이메일 서버에 연결할 수 없거나, 잘못된 이메일 주소를 사용하거나, 인증 오류가 발생하는 등 오류
            throw new MailHandler(EMAIL_SEND_FAIL);
        }
        redisService.setDataExpire(toMail, Integer.toString(authNumber), 60*5L);
    }

}