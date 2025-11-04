package projecct.pyeonhang.common.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.web.filter.GenericFilterBean;
import projecct.pyeonhang.common.utils.JWTUtils;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {
    private final JWTUtils jwtUtils;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        process((HttpServletRequest) request, (HttpServletResponse) response, chain);

    }


    private void process(HttpServletRequest request,
                         HttpServletResponse response,
                         FilterChain chain ) throws IOException, ServletException {


        //요청한 경로 가져오기
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();


        //경로에 로그아웃이 없다면
        if(!requestURI.contains("logout") ||
                !requestMethod.equalsIgnoreCase("POST")){

            chain.doFilter(request, response);
            return;
        }

        String refreshToken ="";
        Cookie[] cookies = request.getCookies();

        response.setContentType("application/json");

        try{

            if(cookies == null) {
                throw new IllegalStateException("쿠기에 정보 없음");
            }

            refreshToken = Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals("refresh"))
                    .map(Cookie::getValue)
                    .findAny().orElseThrow(()-> new IllegalStateException("없음"));

            if( jwtUtils.getExpired(refreshToken)) {
                throw new IllegalStateException("refresh token 유효기간 지남 ");
            }

            String category = jwtUtils.getCategory(refreshToken);

            if(!category.equals("refresh") ){
                throw new IllegalStateException("맞지않는 키입니다.,");
            }


            //Refresh 토큰 Cookie 값 0
            Cookie cookie = new Cookie("refresh", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");

            response.addCookie(cookie);
            response.setStatus(HttpServletResponse.SC_OK);

            JSONObject obj = new JSONObject();

            obj.put("resultMsg", "200");
            obj.put("status", HttpServletResponse.SC_OK);


            response.getWriter().write(obj.toString());

        }catch(Exception e) {

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JSONObject obj = new JSONObject();
            obj.put("resultMsg", "FAIL");
            obj.put("status", HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(obj.toString());

        }

    }



}
