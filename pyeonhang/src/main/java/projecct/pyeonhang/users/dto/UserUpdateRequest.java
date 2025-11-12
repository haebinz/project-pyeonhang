    package projecct.pyeonhang.users.dto;

    import lombok.Data;

    @Data
    public class UserUpdateRequest {
        private String userId;
        private String userName;
        private String passwd;
        private String email;
        private String phone;
        private String birth;
        private String nickname;

    }
