package projecct.pyeonhang.users.dto;


import lombok.*;
import projecct.pyeonhang.users.entity.UsersEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String userId;
    private String passwd;
    private String userName;
    private String nickname;
    private String email;
    private String phone;
    private String birth;
    private String userRole;
    private int pointBalance;
    private LocalDateTime lastModifiedDate;
    private LocalDateTime createDate;


    public static UserDTO of(UsersEntity entity){
        LocalDateTime lastModifiedDate =
                entity.getUpdateDate() == null ? entity.getCreateDate() : entity.getUpdateDate();
        return UserDTO.builder()
                .userId(entity.getUserId())
                .passwd(entity.getPasswd())
                .userName(entity.getUserName())
                .nickname(entity.getNickname())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .birth(entity.getBirth())
                .lastModifiedDate(lastModifiedDate)
                .createDate(entity.getCreateDate())
                .pointBalance(entity.getPointBalance())
                .build();
    }

    public static UsersEntity to(UserDTO dto){

        UsersEntity entity = new UsersEntity();
        entity.setUserId(dto.userId);
        entity.setPasswd(dto.passwd);
        entity.setUserName(dto.userName);
        entity.setNickname(dto.nickname);
        entity.setEmail(dto.email);
        entity.setPhone(dto.phone);
        entity.setBirth(dto.birth);
        entity.setPointBalance(dto.pointBalance);
        return entity;
    }
}
