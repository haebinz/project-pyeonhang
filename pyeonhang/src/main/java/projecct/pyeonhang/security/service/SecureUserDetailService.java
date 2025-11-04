package projecct.pyeonhang.security.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import projecct.pyeonhang.security.dto.SecureUserDTO;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UsersRepository;

@Service
@RequiredArgsConstructor
public class SecureUserDetailService implements UserDetailsService {
    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsersEntity entity =
                usersRepository.findById(username)
                        .orElseThrow(() -> new RuntimeException("사용자 없음"));


        return new SecureUserDTO(entity.getUserId(), entity.getUserName(),
                entity.getPasswd(), entity.getRole().getRoleId()) ;
    }
}
