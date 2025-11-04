package projecct.pyeonhang.users.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserPasswordResetRequest {

    @NotBlank(message="새 비밀번호를 입력해주세요")
    private String newPassword;
    @NotBlank(message="새 비밀번호 확인을 해주세요")
    private String confirmNewPassword;
}
