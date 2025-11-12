package projecct.pyeonhang.users.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserPasswordCodeRequest {
    @NotBlank(message = "아이디를 입력해주세요")
    private String userId;

    @NotBlank(message = "이메일을 입력해주세요")
    private String email;

    @NotBlank(message = "인증 코드를 입력해주세요")
    private String code;


}
