package projecct.pyeonhang.email.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import projecct.pyeonhang.common.dto.ApiResponse;
import projecct.pyeonhang.email.service.PasswordResetService;
import projecct.pyeonhang.users.dto.UserPasswordCodeRequest;
import projecct.pyeonhang.users.dto.UserPasswordRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @PostMapping("/user/password/verify")
    public ResponseEntity<?> sendEmail(@Valid @ModelAttribute UserPasswordRequest request) {
        passwordResetService.requestPasswordReset(request.getUserId(), request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("인증 코드 전송됨"));
    }

    @PostMapping("/user/password/confirm-code")
    public ResponseEntity<?> verifyCode(
            @Valid @ModelAttribute UserPasswordCodeRequest request,
            HttpSession session
    ) {
        if (passwordResetService.verifyCode(request.getUserId(), request.getEmail(), request.getCode())) {
            passwordResetService.markVerified(request.getUserId(), request.getEmail(), request.getCode());
            session.setAttribute("PWD_RESET_USER", request.getUserId());
            return ResponseEntity.ok(ApiResponse.ok("코드 인증 성공"));
        }

        return ResponseEntity.badRequest().body(ApiResponse.fail("코드 인증 실패"));
    }
}
