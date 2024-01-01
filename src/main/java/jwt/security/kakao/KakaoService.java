package jwt.security.kakao;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import jwt.security.config.exception.handler.TokenHandler;
import jwt.security.kakao.dto.KakaoUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static jwt.security.config.code.status.ErrorStatus.KAKAO_TOKEN_RECEIVE_FAIL;
import static jwt.security.config.code.status.ErrorStatus.TOKEN_NOT_FOUND;

@Slf4j
@Service
public class KakaoService {


    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String KAKAO_TOKEN_URL;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String KAKAO_USER_INFO_URL;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String REDIRECT_URL;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String KAKAO_API_KEY;

    public String getAccessToken(String authorizationCode) {
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("client_id", KAKAO_API_KEY);
        map.add("redirect_uri", REDIRECT_URL);
        map.add("code", authorizationCode);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);
        Map<String, Object> response = null;
        try {
            // 카카오 서버 호출하여 값 받음
            response = restTemplate.postForObject(KAKAO_TOKEN_URL, requestEntity, HashMap.class);
            log.info("response is {}", response);
            if(response != null && response.containsKey("access_token")) {
                return response.get("access_token").toString();
            } else {
                // 토큰이 없는 경우 예외 처리
                throw new TokenHandler(TOKEN_NOT_FOUND);
            }
        } catch (RestClientException e) {
            // RestClientException 예외 처리
            log.error("RestClientException 발생: {}", e.getMessage());
            throw new TokenHandler(KAKAO_TOKEN_RECEIVE_FAIL);
        }
    }

    public KakaoUserInfo getUserInfo(String accessToken){
        RestTemplate restTemplate = new RestTemplate();
        log.info("getUserInfo accessToken is {}", accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);

        String response = restTemplate.postForObject(KAKAO_USER_INFO_URL, requestEntity, String.class);
        JsonObject rootObject = JsonParser.parseString(response).getAsJsonObject();
        JsonObject properties = rootObject.getAsJsonObject("properties");
        JsonObject accountObject = rootObject.getAsJsonObject("kakao_account");

        log.info("response is {}", response);
        KakaoUserInfo kakaoUserInfo = KakaoUserInfo.builder()
                .id(rootObject.get("id").getAsString())
                .nickname(properties.get("nickname").getAsString())
                .email(accountObject.get("email").getAsString())
                .build();
        log.info("kakaoUserInfo is {}", kakaoUserInfo);
        return kakaoUserInfo;

    }

}

