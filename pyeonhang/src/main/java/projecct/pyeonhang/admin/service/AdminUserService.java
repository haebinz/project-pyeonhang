package projecct.pyeonhang.admin.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.admin.dto.AdminUserDTO;
import projecct.pyeonhang.admin.dto.AdminUserProjection;
import projecct.pyeonhang.admin.dto.AdminUserSearchDTO;
import projecct.pyeonhang.common.dto.PageVO;
import projecct.pyeonhang.point.entity.PointsEntity;
import projecct.pyeonhang.point.repository.PointsRepository;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UserRoleRepository;
import projecct.pyeonhang.users.repository.UsersRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService  {

    private final UsersRepository usersRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final PointsRepository pointsRepository;

    @Transactional
    public Map<String,Object> getUserList(Pageable pageable, AdminUserSearchDTO searchDTO) throws Exception{
        Map<String,Object> resultMap = new HashMap<>();

        Page<UsersEntity> pageList =
                usersRepository.findAll(pageable);

        List<AdminUserDTO> list = pageList.getContent()
                .stream().map(AdminUserDTO::of).toList();

        PageVO pageVO = new PageVO();
        pageVO.setData(pageList.getNumber(), (int)pageList.getTotalElements());

        resultMap.put("total", pageList.getTotalElements());
        resultMap.put("content", list);
        resultMap.put("pageHTML", pageVO.pageHTML());
        resultMap.put("page", pageList.getNumber());

        return resultMap;
    }

    @Transactional
    public AdminUserDTO getUser(String userId) throws Exception{
        AdminUserProjection user = usersRepository.getUserById(userId)
                .orElseThrow(()->new RuntimeException("사용자가 존재하지 않음"));

        return AdminUserDTO.of(user);
    }

    @Transactional
    public Map<String, Object> grantPoints(String userId, int amount, String reason) {
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않음"));

        int pointBalance = user.getPointBalance() == null ? 0 : user.getPointBalance();

        // 0 미만 방지(클램프)
        int target = pointBalance + amount;
        int after = Math.max(0, target);
        int newPoint = after - pointBalance;
        user.setPointBalance(after);
        usersRepository.save(user);

        // 2) 포인트 이력 저장 (ADMIN_GRANT)
        PointsEntity entity = PointsEntity.builder()
                .user(user)
                .sourceType(PointsEntity.SourceType.ADMIN_GRANT)
                .amount(newPoint)
                .reason(reason)
                .build();
        pointsRepository.save(entity);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("userId", userId);
        resultMap.put("이전포인트", pointBalance);
        resultMap.put("지급할포인트", amount);
        resultMap.put("지급 후 총 포인트", after);
        if (reason != null && !reason.isBlank()) resultMap.put("지급사유", reason);
        return resultMap;
    }

    @Transactional
    public Map<String, Object> updateUserUseYn(String userId, String useYn) {
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않음"));

        String activate = "Y".equalsIgnoreCase(useYn) ? "Y" : "N";
        String status = user.getUseYn();
        user.setUseYn(activate);
        usersRepository.save(user);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("userId", userId);
        resultMap.put("이전 상태", status);
        resultMap.put("useYn사용", activate);
        resultMap.put("active", "Y".equals(activate));
        return resultMap;
    }




}
