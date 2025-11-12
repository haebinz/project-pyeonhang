package projecct.pyeonhang.admin.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import projecct.pyeonhang.users.entity.UsersEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminUserDTO {

    private String userId;
    private String passwd;
    private String userName;
    private String nickname;
    private String birth;
    private String email;
    private String phone;
    private String delYn;
    private String userRole;
    private String roleName;
    private Integer pointBalance;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

    public static AdminUserDTO of(UsersEntity entity){
        return AdminUserDTO
                .builder()
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .nickname(entity.getNickname())
                .birth(entity.getBirth())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .pointBalance(entity.getPointBalance())
                .delYn(entity.getDelYn())
                .createDate(entity.getCreateDate())
                .updateDate(entity.getUpdateDate())
                .userRole(entity.getRole().getRoleId())
                .roleName(entity.getRole().getRoleName())
                .build();
    }


    public static AdminUserDTO of(AdminUserProjection entity) {
        return AdminUserDTO
                .builder()
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .birth(entity.getBirth())
                .nickname(entity.getNickname())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .pointBalance(entity.getPointBalance())
                .delYn(entity.getDelYn())
                .createDate(entity.getCreateDate())
                .updateDate(entity.getUpdateDate())
                .userRole(entity.getRoleId())
                .roleName(entity.getRoleName())
                .build();
    }



    public static UsersEntity to(AdminUserDTO dto) {
        UsersEntity entity = new UsersEntity();
        entity.setUserId(dto.getUserId());
        entity.setPasswd(dto.getPasswd());
        entity.setUserName(dto.getUserName());
        entity.setBirth(dto.getBirth());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setNickname(dto.getNickname());
        entity.setPointBalance(dto.getPointBalance());
        entity.setDelYn(dto.getDelYn() == null ?  "N" : dto.getDelYn());

        return entity;
    }



}
