package projecct.pyeonhang.common.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import projecct.pyeonhang.users.dto.UserSecureDTO;

import java.io.IOException;

public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    //http 요청을 기억하는 객체
    private RequestCache requestCache = new HttpSessionRequestCache();
    //응답 전략
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    //세터를 통한 의존성 주입
    public void setRequestCache(RequestCache requestCache) {

        this.requestCache = requestCache;
    }

    //권한 처리 후 실행
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {


        //로그인 성공 후 처리
        setDefaultTargetUrl("/main");
        //이동경로가 있으면 우선사용
        setAlwaysUseDefaultTargetUrl(false);

        request.getSession().setMaxInactiveInterval(1800);
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        //세션  가져오기
        HttpSession session = request.getSession();
        //사용자 정보 저장
        session.setAttribute("user", (UserSecureDTO)authentication.getPrincipal());

        //이전에 저장된 요청 경로가 있을 경우 처리
        if(savedRequest != null) {
            String targetURI = savedRequest.getRedirectUrl();

            if(targetURI.contains("error") || targetURI.contains(".well-known")
                    || targetURI.contains("login")) {
                targetURI = getDefaultTargetUrl();
            }
            redirectStrategy.sendRedirect(request, response, targetURI);
        }else {
            redirectStrategy.sendRedirect(request, response,  getDefaultTargetUrl());
        }
    }
}
