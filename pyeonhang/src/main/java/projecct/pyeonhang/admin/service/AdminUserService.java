package projecct.pyeonhang.admin.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.admin.dto.AdminUserDTO;
import projecct.pyeonhang.admin.dto.AdminUserProjection;
import projecct.pyeonhang.admin.dto.AdminUserSearchDTO;
import projecct.pyeonhang.board.entity.BoardEntity;
import projecct.pyeonhang.board.repository.BoardRepository;
import projecct.pyeonhang.common.dto.PageVO;
import projecct.pyeonhang.point.entity.PointsEntity;
import projecct.pyeonhang.point.repository.PointsRepository;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UsersRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService  {

    private final UsersRepository usersRepository;
    private final PointsRepository pointsRepository;
    private final BoardRepository boardRepository;

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

    //게시글 관리->게시글 채택
    @Transactional
    public Map<String,Object> bestBoard(String adminUserId, List<Integer> brdIdList) {

        Map<String,Object> resultMap = new HashMap<>();


        UsersEntity admin = usersRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않음"));

        if (admin.getRole() == null || admin.getRole().getRoleId() == null) {
            resultMap.put("resultCode", 403);
            resultMap.put("resultMessage", "권한 정보가 없습니다.");
            return resultMap;
        }

        String roleId = admin.getRole().getRoleId().toUpperCase();

        if (!roleId.equals("ADMIN") && !roleId.equals("ROLE_ADMIN")) {
            resultMap.put("resultCode", 403);
            resultMap.put("resultMessage", "관리자만 게시글을 채택할 수 있습니다.");
            return resultMap;
        }

        List<Integer> existingBrdIds = validateBoards(brdIdList);
        
        for (Integer brdId : existingBrdIds) {
            BoardEntity board = boardRepository.findById(brdId)
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. ID: " + brdId));
            
            if ("Y".equals(board.getBestYn())) { 
                continue; // 이미 채택된 게시글은 채택하지 않고 다음으로
            }

            // 실제 채택 처리
            board.setBestYn("Y");
            boardRepository.save(board);
        }

        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "게시글이 채택되었습니다.");
        return resultMap;
    }
    
    //게시글 공지로 등록
    @Transactional
    public Map<String,Object> noticeBoard(String adminUserId, List<Integer> brdIdList) {

        Map<String,Object> resultMap = new HashMap<>();


        UsersEntity admin = usersRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않음"));

        if (admin.getRole() == null || admin.getRole().getRoleId() == null) {
            resultMap.put("resultCode", 403);
            resultMap.put("resultMessage", "권한 정보가 없습니다.");
            return resultMap;
        }

        String roleId = admin.getRole().getRoleId().toUpperCase();

        if (!roleId.equals("ADMIN") && !roleId.equals("ROLE_ADMIN")) {
            resultMap.put("resultCode", 403);
            resultMap.put("resultMessage", "관리자만 공지글로 선택할 수 있습니다.");
            return resultMap;
        }

        List<Integer> existingBrdIds = validateBoards(brdIdList);

        for (Integer brdId : existingBrdIds) {
            BoardEntity board = boardRepository.findById(brdId)
                    .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않음"));

            if ("Y".equals(board.getNoticeYn())) { 
                board.setNoticeYn("N");
                boardRepository.save(board);
                continue;
            }

            board.setNoticeYn("Y");
            boardRepository.save(board);

        }

        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "게시글 공지 설정이 완료되었습니다.");

        return resultMap;
    }


    //게시글 삭제
    public Map<String,Object> deleteBoard(String adminUserId, List<Integer> brdIdList){
        Map<String,Object> resultMap = new HashMap<>();

        if (adminUserId == null || adminUserId.isBlank()) {
            throw new RuntimeException("로그인이 필요한 서비스입니다");
        }

        UsersEntity admin = usersRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다"));

        String roleId = (admin.getRole() != null && admin.getRole().getRoleId() != null)
                ? admin.getRole().getRoleId().toUpperCase()
                : "";

        boolean isAdmin = roleId.equals("ADMIN") || roleId.equals("ROLE_ADMIN");

        if (!isAdmin) {
            throw new RuntimeException("관리자만 사용할 수 있는 기능입니다.");
        }

        List<Integer> existingBrdIds = validateBoards(brdIdList);

        // 배열 한번에 삭제
        boardRepository.deleteAllById(existingBrdIds);

        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "SUCCESS");
        resultMap.put("deletedBoardId", existingBrdIds);
        resultMap.put("deletedBy", adminUserId);

        return resultMap;

    }


    // 요청 받은 게시글 리스트 존재하는지 확인
    private List<Integer> validateBoards(List<Integer> brdIdList) {
        
        // 게시글이 존재하는 brdId 리스트
        List<Integer> existingBrdIds = boardRepository.findAllByBrdIdIn(brdIdList).stream().map(BoardEntity::getBrdId).toList();
        
        // 존재하지 않는 brdId 리스트
        List<Integer> notFoundIds = brdIdList.stream()
                                     .filter(id -> !existingBrdIds.contains(id))
                                     .toList();        

        if (!notFoundIds.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 게시물 ID: " + notFoundIds);
        }

        return existingBrdIds;

    }

}
