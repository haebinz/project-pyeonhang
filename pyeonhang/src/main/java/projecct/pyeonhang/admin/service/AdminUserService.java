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
    private final PointsRepository pointsRepository;

    //사용자 리스트 가져오기 + 검색 기능
    @Transactional
    public Map<String,Object> getUserList(Pageable pageable, AdminUserSearchDTO searchDTO) throws Exception{
        Map<String,Object> resultMap = new HashMap<>();

        String role = (searchDTO != null && searchDTO.getRoleFilter() != null && !searchDTO.getRoleFilter().isBlank())
                ? searchDTO.getRoleFilter().trim()
                : null;

        String delyn = (searchDTO != null && searchDTO.getDelYn() != null && !searchDTO.getDelYn().isBlank())
                ? searchDTO.getDelYn().trim()
                : null;

        String search = (searchDTO != null && searchDTO.getSearchText() != null && !searchDTO.getSearchText().isBlank())
                ? searchDTO.getSearchText().trim()
                : null;

        Page<UsersEntity> pageList =
                usersRepository.findAllByRoleAndSearchAndDelYn(role, delyn, search, pageable);

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
    //관리자 페이지->사용자 delYn수정
    public Map<String,Object> changeUserDelYn(String userId){
        Map<String,Object> resultMap = new HashMap<>();
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않음"));
        try{
            user.setDelYn("Y");
            usersRepository.save(user);
            resultMap.put("success",true);
            resultMap.put("변경된 상태",user.getDelYn());
        }catch (Exception e){
            resultMap.put("변경실패",false);

        }
        return resultMap;
    }

    //특정 사용자 정보 가져오기
    @Transactional
    public AdminUserDTO getUser(String userId) throws Exception{
        AdminUserProjection user = usersRepository.getUserById(userId)
                .orElseThrow(()->new RuntimeException("사용자가 존재하지 않음"));

        return AdminUserDTO.of(user);
    }
    //유저한테 포인트 주기
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





}
