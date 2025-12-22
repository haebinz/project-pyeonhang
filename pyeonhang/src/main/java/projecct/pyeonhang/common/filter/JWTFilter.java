package projecct.pyeonhang.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import projecct.pyeonhang.common.utils.JWTUtils;
import projecct.pyeonhang.users.dto.UserSecureDTO;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtils jwtUtils;

     private static final List<String> PERMIT_PATHS = List.of("/api/v1/user/login", "/api/v1/user/add", "/api/v1/board", "/api/v1/crawl/**", "/api/v1/refresh", "/api/v1/email", "/api/v1/email/**", "/favicon.ico");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if ("OPTIONS".equals(request.getMethod())) {
        filterChain.doFilter((ServletRequest)request, (ServletResponse)response);
        return;
        } 

        if (isPermitPath(path)) {
        filterChain.doFilter((ServletRequest)request, (ServletResponse)response);
        return;
        } 
        
        //요청 request header 에서 token 찾기
        //헤더에 원래 있는 속성임....
        String acessToken = request.getHeader("Authorization");

        if(acessToken == null || !acessToken.startsWith("Bearer ")) {
            response.setStatus(401);
            return;
        }

        try{

            if(acessToken.startsWith("Bearer ")) {
                acessToken = acessToken.substring(7);

                if( !jwtUtils.validateToken(acessToken)) {
                    throw new IllegalAccessException("유효하지 않은 토큰입니다.");
                }
            }

            String category = this.jwtUtils.getCategory(acessToken);
            if (!category.equals("access"))
                throw new IllegalAccessException("유효하지 않은 토큰입니다."); 
        } catch (Exception e) {
            PrintWriter writer = response.getWriter();
            writer.println("유효하지 않은 토큰입니다.");
            response.setStatus(401);
            return;
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


    private boolean isPermitPath(String path) {
        AntPathMatcher matcher = new AntPathMatcher();
        return PERMIT_PATHS.stream()
        .anyMatch(pattern -> matcher.match(pattern, path));
    }    


}
