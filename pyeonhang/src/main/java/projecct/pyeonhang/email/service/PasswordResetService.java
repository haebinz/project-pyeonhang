package projecct.pyeonhang.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.common.utils.HashUtils;
import projecct.pyeonhang.email.entity.EmailEntity;
import projecct.pyeonhang.email.repository.EmailRepository;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UsersRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UsersRepository usersRepository;
    private final EmailRepository emailRepository;
    private final EmailService emailService;

    @Value("${app.pwd-reset.code-ttl-seconds:600}")
    private long codeTtlSeconds;

    @Transactional
    public void requestPasswordReset(String userId, String email) {

        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("아이디 또는 이메일이 올바르지 않습니다."));

        if (!user.getEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("아이디 또는 이메일이 올바르지 않습니다.");
        }

        String code = emailService.createCode();
        String codeHash = HashUtils.sha256Hex(code);

        EmailEntity emailEntity = new EmailEntity();
        emailEntity.setUser(user);
        emailEntity.setEmail(email);
        emailEntity.setCodeHash(codeHash);
        emailEntity.setExpiresAt(LocalDateTime.now().plusSeconds(codeTtlSeconds));

        emailRepository.save(emailEntity);
        emailService.sendAuthMail(email, code);
    }

    @Transactional(readOnly = true)
    public boolean verifyCode(String userId, String email, String code) {
        UsersEntity user = usersRepository.findById(userId).orElseThrow();

        EmailEntity emailEntity = emailRepository
                .findTopByUserAndEmailOrderByCreatedAtDesc(user, email)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 기록 없음."));

        if (emailEntity.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("인증 코드가 만료됨.");

        return HashUtils.sha256Hex(code).equals(emailEntity.getCodeHash());
    }

    @Transactional
    public void markVerified(String userId, String email, String code) {
        UsersEntity user = usersRepository.findById(userId).orElseThrow();
        EmailEntity emailEntity = emailRepository
                .findTopByUserAndEmailOrderByCreatedAtDesc(user, email)
                .orElseThrow();

        emailEntity.setVerifiedYn("Y");
        emailRepository.save(emailEntity);
    }
}
