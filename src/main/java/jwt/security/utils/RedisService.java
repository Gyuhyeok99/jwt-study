package jwt.security.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${jwt.refresh-token.expiration}")
    private Long refreshExpiration;

    public void expireValues(String key) {
        redisTemplate.expire(key, refreshExpiration, TimeUnit.MILLISECONDS);
    }

    public void setValueOps(String key, String value) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value);
    }

    @Transactional(readOnly = true)
    public String getValueOps(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void deleteValueOps(String key) {
        redisTemplate.delete(key);
    }

    public void setDataExpire(String key,String value,long duration){
        ValueOperations<String,Object> valueOperations= redisTemplate.opsForValue();
        Duration expireDuration=Duration.ofSeconds(duration);
        valueOperations.set(key,value,expireDuration);
    }


}
