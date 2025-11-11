package projecct.pyeonhang.email.controller;

import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import projecct.pyeonhang.email.service.EmailService;

@RestController
@RequestMapping("api/v1")
public class EmailAPIController {
    private final EmailService emailService;

    public EmailAPIController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/email/auth/{email}")
    public ResponseEntity<String> requestAuthcode(@PathVariable String email) throws MessagingException {
        boolean isSend = emailService.sendSimpleMessage(email);
        return isSend ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 코드가 전송되었습니다.") :
                ResponseEntity.status(HttpStatus.OK).body("인증 코드 발급에 실패하였습니다.");
    }
}
