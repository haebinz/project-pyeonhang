package projecct.pyeonhang.common.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
@Slf4j
public class JWTUtils {

    private SecretKey secretKey;

    public JWTUtils(@Value("${spring.jwt.secretKey}") String secret) {
        // JWT 토큰을 만들기 위한 비공개키를 h256 알고리즘을 통해 생성
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    //사용자 아이디, 권한, 이름, 탈퇴여부, 지속시간(분)
    public String createJwt(String category, String userId,
                            String userName, String userRole,String nickname, String userDelYn, Long mins) {

        return Jwts.builder()
                .claim("category", category)
                .claim("userId", userId)
                .claim("userName", userName)
                .claim("userName", nickname)
                .claim("userRole", userRole)
                .claim("userDelYn", userDelYn)
                .issuedAt(Timestamp.valueOf(LocalDateTime.now()))
                .expiration(Timestamp.valueOf( LocalDateTime.now().plusMinutes(mins) ))
                .signWith(secretKey)
                .compact();
    }


    //JWT 토큰 유효성 체크
    public boolean validateToken(String token) {
        try{
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        }catch(SecurityException | MalformedJwtException e) {
            log.error("유효하지 않은 JWT 서명입니다.");
        }catch(ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        }catch(UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        }catch(IllegalArgumentException e) {
            log.error("잘못된  JWT 토큰입니다");
        }
        return false;
    }


    //토큰 카데고리 분석
    public String getCategory(String token) {

        return Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().get("category", String.class);
    }

    //아이디 추출
    public String getUserId(String token) {

        return Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().get("userId", String.class);
    }


    //이름 추출
    public String getUserName(String token) {

        return Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().get("userName", String.class);
    }


    //권한 추출
    public String gertUserRole(String token) {

        return Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().get("userRole", String.class);
    }
    //닉네임 추출
    public String getNickname(String token) {

        return Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().get("nickname", String.class);
    }

    // 삭제 여부 추출(로그인 시 탈퇴한 회원 막기 위해)
    public String getDelYn(String token) {

        return Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().get("userDelYn", String.class);
    }


    //유효시간 체크
    public boolean getExpired(String token) {

        //현재 시간이 유효시간보다 이전인지 체크
        return Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().getExpiration().before(Timestamp.valueOf(LocalDateTime.now()));
    }
}
