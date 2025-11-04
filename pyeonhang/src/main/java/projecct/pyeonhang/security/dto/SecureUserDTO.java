package projecct.pyeonhang.security.dto;


import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;

public class SecureUserDTO extends User {
    private static final String ROLE_PREFIX = "ROLE_";

    @Getter
    private String userId;
    @Getter
    private String userName;

    public SecureUserDTO(String userId, String userName, String password, String roleName) {
        super(userId, password, getAuthority(roleName));

        this.userId = userId;
        this.userName = userName;
    }


    private static List<GrantedAuthority> getAuthority(String roleName){
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority(ROLE_PREFIX + roleName));
        return list;
    }
}
