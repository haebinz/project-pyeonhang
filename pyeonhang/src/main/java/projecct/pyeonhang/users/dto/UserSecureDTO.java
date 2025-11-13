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
    private String delYn;
    private String nickname;     

    public UserSecureDTO(String userId, String userName, String passwd, String userRole, String delYn,String nickname) {
        super(userId, passwd, makeGrantedAuthorities(userRole));

        this.userId = userId;
        this.userName = userName;
        this.delYn = delYn;
        this.nickname = nickname;        
    }

    @Override
    public boolean isEnabled() {
        return !"Y".equals(delYn); // delYn이 'Y'면 false 반환 -> 로그인 실패
    }    

    //생성자에서 사용해야하기 때문에 static 으로 처리
    private static List<GrantedAuthority> makeGrantedAuthorities(String userRole) {
        List<GrantedAuthority> list = new ArrayList<>();

        list.add(new SimpleGrantedAuthority(ROLE_PREFIX + userRole ));
        return list;
    }
}
