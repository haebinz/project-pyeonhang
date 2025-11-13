package projecct.pyeonhang.users.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import projecct.pyeonhang.users.dto.UserSecureDTO;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UsersRepository;

@Service
@RequiredArgsConstructor
public class UserServiceDetails implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UsersEntity user =
                usersRepository.findById(username)
                        .orElseThrow(() -> new UsernameNotFoundException(username + "을 찾을 수 없습니다."));

        return new UserSecureDTO(user.getUserId(), user.getUserName(),
                user.getPasswd(), user.getRole().getRoleId(), user.getDelYn(),user.getNickname());
    }
}
