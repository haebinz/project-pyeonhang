package projecct.pyeonhang.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import projecct.pyeonhang.common.utils.JWTUtils;
import projecct.pyeonhang.users.dto.UserSecureDTO;

import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        //요청 request header 에서 token 찾기
        //헤더에 원래 있는 속성임....
        String acessToken = request.getHeader("Authorization");

        if(acessToken == null) {
            log.info("acessToken is null");
            filterChain.doFilter(request, response);
            return; // 자격이 없으니 로그인으로 넘거가라!
        }

        try{

            if(acessToken.startsWith("Bearer ")) {
                acessToken = acessToken.substring(7);

                if( !jwtUtils.validateToken(acessToken)) {
                    throw new IllegalAccessException("유효하지 않은 토큰입니다.");
                }
            }

            //토큰의 카테고리 검색
            String category = jwtUtils.getCategory(acessToken);

            if(! category.equals("access")) {
                throw new IllegalAccessException("유효하지 않은 토큰입니다.");
            }

        }catch(Exception e) {
            //response 한다!
            PrintWriter writer = response.getWriter();
            writer.println("토큰이 유효하지 않습니다.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;  // 함수 종료
        }

        // 인증 성공
        String userId = jwtUtils.getUserId(acessToken);
        String userName = jwtUtils.getUserName(acessToken);
        String userRole = jwtUtils.gertUserRole(acessToken);
        String delYn = jwtUtils.getDelYn(acessToken);
        String nickname = jwtUtils.getNickname(acessToken);


        UserSecureDTO dto = new UserSecureDTO(userId, userName, userName, userRole, delYn, nickname);

        //시큐리티 세션에 저장()
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(dto, null, dto.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        //다음으로 이동
        filterChain.doFilter(request, response);

    }


}
