package projecct.pyeonhang.users.service;



import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.users.dto.*;
import projecct.pyeonhang.users.entity.UserRoleEntity;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UserRoleRepository;
import projecct.pyeonhang.users.repository.UsersRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    //사용자 추가
    public void addUser(UserRequest userRequest) throws Exception{
        boolean exists = usersRepository.existsByUserId(userRequest.getUserId());
        if (exists) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

        UsersEntity entity = new UsersEntity();
        entity.setUserId(userRequest.getUserId());
        entity.setUserName(userRequest.getUserName());
        entity.setPasswd(passwordEncoder.encode(userRequest.getPasswd()));
        entity.setEmail(userRequest.getEmail());
        entity.setPhone(userRequest.getPhone());
        entity.setNickname(userRequest.getNickname());
        entity.setBirth(userRequest.getBirth());
        entity.setRole(userRequest.getRole());
        UserRoleEntity role = userRoleRepository.findById("USER")
                .orElseThrow(() -> new RuntimeException("기본 권한(USER)이 없습니다."));
        entity.setRole(role);

        usersRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public UserDTO findMe(String userId) {
        UsersEntity entity = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        UserDTO dto = UserDTO.of(entity);
        dto.setPasswd(null);
        return dto;
    }

    //사용자 정보 수정
    @Transactional
    public void updateUser(String userId,UserUpdateRequest request) {
        UsersEntity entity = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        // null 아닌 값만 반영
        if (request.getUserName() != null) entity.setUserName(request.getUserName());
        if (request.getBirth()    != null) entity.setBirth(request.getBirth());
        if (request.getPhone()    != null) entity.setPhone(request.getPhone());
        if (request.getEmail()    != null) entity.setEmail(request.getEmail());
        if (request.getNickname()    != null) entity.setNickname(request.getNickname());
        //if (request.getPasswd()    != null) entity.setPasswd(passwordEncoder.encode(request.getPasswd()));


        usersRepository.save(entity);
    }
    //비밀번호 수정
    @Transactional
    public void changeMyPassword(String userId, UserPasswordResetRequest request) {
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
          
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 확인이 일치하지 않습니다.");
        }

        user.setPasswd(passwordEncoder.encode(request.getNewPassword()));
    }


    //사용자 아이디 찾기
    @Transactional(readOnly = true)
    public Map<String, Object> findUserId(String userName, String email) throws Exception{

        String userId = usersRepository.findUserIdByUserNameAndEmail(userName, email)
                .orElseThrow(() -> new RuntimeException("조회된 정보가 없습니다.다시입력해주세요"));

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "OK");
        resultMap.put("data", userId);
        return resultMap;
    }

    //사용자 비밀번호 찾기->아이디 일치하는지 확인
    @Transactional(readOnly = true)
    public Map<String,Object> findUserByUserIdAndEmail(String userId, String email) {
        UsersEntity user = usersRepository.findByUserIdAndEmail(userId, email)
                .orElseThrow(() -> new RuntimeException("정보가 없습니다.다시입력해주세요"));

        Map<String,Object> result = new HashMap<>();
        result.put("resultCode", 200);
        result.put("resultMessage", "OK");
        return result;
    }

    @Transactional(readOnly = true)
    public void verifyUserIdAndEmail(String userId, String email) {
        usersRepository.findByUserIdAndEmail(userId, email)
                .orElseThrow(() -> new RuntimeException("아이디 또는 이메일이 일치하지 않습니다."));
    }

    @Transactional
    public void resetPasswordForUserId(String userId, String newPassword, String confirmNewPassword) {
        if (!newPassword.equals(confirmNewPassword)) {
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setPasswd(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
    }










}
