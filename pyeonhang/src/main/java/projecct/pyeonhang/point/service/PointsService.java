package projecct.pyeonhang.point.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.point.dto.PointsDTO;
import projecct.pyeonhang.point.repository.PointsRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointsRepository pointsRepository;

    //마이페이지->포인트 리스트
    @Transactional(readOnly = true)
    public Map<String, Object> listMyPoints(String userId) {
        List<PointsDTO> items = pointsRepository.findAllByUserIdWithUser(userId)
                .stream()
                .map(p -> PointsDTO.builder()
                        .id(p.getId())
                        .amount(p.getAmount())
                        .reason(p.getReason() != null ? p.getReason() : null)
                        .sourceType(p.getSourceType() != null ? p.getSourceType().name() : null)
                        .createDate(p.getCreateDate())
                        .build())
                .toList();

        int balance = pointsRepository.sumByUserId(userId);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("balance", balance); // 총합
        resultMap.put("items", items);     // 내역
        return resultMap;
    }
}
