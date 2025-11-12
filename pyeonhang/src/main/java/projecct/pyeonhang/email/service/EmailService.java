package projecct.pyeonhang.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final SecureRandom random = new SecureRandom();

    public String createCode() {
        return String.format("%06d", random.nextInt(999999));
    }

    private MimeMessage createMessage(String to, String authCode) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject("[PYEONHANG] 비밀번호 재설정 인증 코드");

        String body = """
                <h3>비밀번호 재설정을 위한 인증코드입니다.</h3>
                <h2>%s</h2>
                <p>10분 안에 입력해주세요.</p>
                """.formatted(authCode);

        helper.setText(body, true);
        return message;
    }

    public boolean sendAuthMail(String to, String code) {
        try {
            javaMailSender.send(createMessage(to, code));
            return true;
        } catch (MailException | MessagingException e) {
            log.error("메일 전송 실패 = {}", e.getMessage());
            return false;
        }
    }
}
