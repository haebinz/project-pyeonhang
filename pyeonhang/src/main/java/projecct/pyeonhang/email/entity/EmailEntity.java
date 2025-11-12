package projecct.pyeonhang.email.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projecct.pyeonhang.users.entity.UsersEntity;

import java.time.LocalDateTime;

@Entity
@Table(name="email")
@Getter
@Setter
@NoArgsConstructor
public class EmailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private UsersEntity user;

    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_yn", columnDefinition = "CHAR(1)")
    private String verifiedYn = "N";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
