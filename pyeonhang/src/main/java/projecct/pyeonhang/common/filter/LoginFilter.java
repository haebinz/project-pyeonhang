package projecct.pyeonhang.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import projecct.pyeonhang.attendance.service.AttendanceService;
import projecct.pyeonhang.common.utils.JWTUtils;
import projecct.pyeonhang.users.dto.UserSecureDTO;

import java.io.IOException;
import java.util.Iterator;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;
    private final AttendanceService attendanceService;

    public static final long ACCESS_TOKEN_EXPIRE_TIME = 86400L;   // 24시간
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 86400L;

    //24시간

    //인증시도
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String username = obtainUsername(request);
        String password = obtainPassword(request);
        
        
        UsernamePasswordAuthenticationToken authRequest =
        UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        
        authRequest.setDetails(request.getParameter("delYn"));
        
        return authenticationManager.authenticate(authRequest);
    }


    //성공처리
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {


        UserSecureDTO user = (UserSecureDTO) authResult.getPrincipal();
        String userId = user.getUserId();
        String userName = user.getUserName();
        String userDelYn = user.getDelYn();
        String nickname = user.getNickname();

        //권한 > 현재 한개니까 하나만 추출하자
        Iterator<? extends GrantedAuthority> iter =  authResult.getAuthorities().iterator();
        String userRole = iter.next().getAuthority();

        //토큰 생성
        String accessToken = jwtUtils.createJwt("access", userId, userName,
                userRole, userDelYn,nickname, ACCESS_TOKEN_EXPIRE_TIME);
        String refreshToken = jwtUtils.createJwt("refresh", userId, userName,
                userRole, userDelYn,nickname, REFRESH_TOKEN_EXPIRE_TIME);

        //응답을 설정
        response.setHeader("Authorization", accessToken);
        response.addCookie(createCookie("refresh", refreshToken));
        response.setStatus(HttpServletResponse.SC_OK);

        try {

            attendanceService.checkAttendance(userId);
        } catch (Exception e) {

            e.printStackTrace();
        }

        try{

            JSONObject jObj = new JSONObject();
            jObj.put("resultMsg", "OK");
            jObj.put("status", "200");

            JSONObject data = new JSONObject();

            data.put("userId", userId);
            data.put("userName", userName);
            data.put("userRole", userRole);
            data.put("token", accessToken);
            data.put("nickname", nickname);

            jObj.put("content", data);

            // response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write(jObj.toString());


        }catch(Exception e) {
            e.printStackTrace();
        }

    }


    //실패처리
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        try{

            JSONObject jObj = new JSONObject();
            jObj.put("resultMsg", "FIAL");
            jObj.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            JSONObject data = new JSONObject();
            jObj.put("content", data);

            response.getWriter().write(jObj.toString());

        }catch(Exception e) {
            e.printStackTrace();
        }

    }


    //토큰 저장
    private Cookie createCookie(String name, Object value) {
        Cookie cookie = new Cookie(name, String.valueOf(value));
        cookie.setPath("/");
        cookie.setMaxAge(30*60);
        cookie.setHttpOnly(true); // 자바스크립트에서 접근 금지

        return cookie;
    }

}
