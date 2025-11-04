package projecct.pyeonhang.users.dto;


import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UserSecureDTO extends User {

    private static final String ROLE_PREFIX = "ROLE_";

    private String userId;
    private String userName;

    public UserSecureDTO(String userId, String userName, String passwd, String userRole) {
        super(userId, passwd, makeGrantedAuthorities(userRole));

        this.userId = userId;
        this.userName = userName;
    }

    //생성자에서 사용해야하기 때문에 static 으로 처리
    private static List<GrantedAuthority> makeGrantedAuthorities(String userRole) {
        List<GrantedAuthority> list = new ArrayList<>();

        list.add(new SimpleGrantedAuthority(ROLE_PREFIX + userRole ));
        return list;
    }
}
