    package projecct.pyeonhang.users.dto;

    import jakarta.validation.constraints.NotBlank;
    import lombok.Data;

    @Data
    public class UserFindRequest {

        @NotBlank(message="이름을 입력해주세요")
        private String userName;
        @NotBlank(message="이메일을 입력해주세요")
        private String email;

    }
