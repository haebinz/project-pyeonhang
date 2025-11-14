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

        // 이메일로 인증코드 보내기
        @GetMapping("/email/auth/{email}")
        public ResponseEntity<String> requestAuthcode(@PathVariable("email") String email) {
            // 1) 코드 생성
            String code = emailService.createCode();

            // 2) 메일 전송 (EmailService.sendAuthMail(to, code))
            boolean sent = emailService.sendAuthMail(email, code);

            if (sent) {
                // 성공: 200 OK
                return ResponseEntity.ok("인증 코드가 전송되었습니다.");
            } else {
                // 실패: 500 Internal Server Error
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("인증 코드 발급에 실패하였습니다.");
            }
        }
    }
