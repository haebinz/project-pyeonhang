package projecct.pyeonhang.common.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import projecct.pyeonhang.attendance.service.AttendanceService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLoginHandler implements AuthenticationSuccessHandler {

    private final AttendanceService attendanceService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        String userId = authentication.getName();
        try{
            attendanceService.checkAttendance(userId);
        }catch(Exception e){
            e.printStackTrace();
        }

        response.sendRedirect("/");
    }
}
