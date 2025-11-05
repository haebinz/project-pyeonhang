package projecct.pyeonhang.admin.dto;

import java.time.LocalDateTime;

public interface AdminUserProjection {

    String getUserId();
    String getUserName();
    String getNickname();
    String getBirth();
    String getPhone();
    String getEmail();
    String getUseYn();
    String getDelYn();
    Integer getPointBalance();
    LocalDateTime getCreateDate();
    LocalDateTime getUpdateDate();
    String getRoleId();
    String getRoleName();
}
