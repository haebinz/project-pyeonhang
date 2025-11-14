package projecct.pyeonhang.users.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import projecct.pyeonhang.users.entity.UserRoleEntity;

@Data
public class UserRequest {
    @NotBlank(message="아이디를 입력해주세요")
    private String userId;
    @NotBlank(message="비밀번호를 입력해주세요")
    private String passwd;
    @NotBlank(message="이름를 입력해주세요")
    private String userName;
    @NotBlank(message="이메일를 입력해주세요")
    private String email;
    @NotBlank(message="휴대폰 번호를 입력해주세요")
    private String phone;
    
    private String nickname;
    @NotBlank(message="생년월일을 입력해주세요")
    private String birth;
    private UserRoleEntity role;

}
