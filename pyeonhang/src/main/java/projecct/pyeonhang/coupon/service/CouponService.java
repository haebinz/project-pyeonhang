package projecct.pyeonhang.coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;

import projecct.pyeonhang.common.dto.PageVO;
import projecct.pyeonhang.common.service.CloudinaryService;
import projecct.pyeonhang.common.utils.FileUtils;
import projecct.pyeonhang.coupon.dto.CouponDTO;
import projecct.pyeonhang.coupon.dto.CouponRequestDTO;
import projecct.pyeonhang.coupon.dto.CouponUpdateDTO;
import projecct.pyeonhang.coupon.entity.CouponEntity;
import projecct.pyeonhang.coupon.repository.CouponRepository;
import projecct.pyeonhang.point.entity.PointsEntity;
import projecct.pyeonhang.point.repository.PointsRepository;
import projecct.pyeonhang.users.dto.UserCouponDTO;
import projecct.pyeonhang.users.entity.UserCouponEntity;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UserCouponRepository;
import projecct.pyeonhang.users.repository.UsersRepository;


import java.io.File;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {
    private final CouponRepository couponRepository;
    private final UsersRepository usersRepository;
    private final PointsRepository pointsRepository;
    private final UserCouponRepository userCouponRepository;
    private final CloudinaryService cloudinaryService;

    //파일 업로드 지정
    @Value("${server.file.coupon.path}")
    private String filePath;

    @Transactional
    public void registerCoupon(CouponRequestDTO request) throws Exception {
        // // 파일 필수
         MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("파일은 필수입니다.");
        }


        // // 업로드
        String cloudinaryId = UUID.randomUUID().toString();
        String imgUrl = cloudinaryService.uploadFile(file, "coupon", cloudinaryId);


        //쿠폰 저장
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName(request.getCouponName());
        couponEntity.setDescription(request.getDescription());
        couponEntity.setRequiredPoint(request.getRequiredPoint());
        couponEntity.setCloudinaryId(cloudinaryId);
        couponEntity.setImgUrl(imgUrl);
        couponRepository.save(couponEntity);
    }

    private long getFileSize(String absolutePath) {
        File f = new File(absolutePath);
        return f.exists() ? f.length() : 0L;
    }
    
    //쿠폰 수정
    @Transactional
    public Map<String,Object> updateCoupon(int couponId, CouponUpdateDTO update) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

            CouponEntity coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다"));

            if (update.getCouponName() != null) {
                coupon.setCouponName(update.getCouponName());
            }
            if (update.getDescription() != null) {
                coupon.setDescription(update.getDescription());
            }
            if (update.getRequiredPoint() != null) {
                coupon.setRequiredPoint(update.getRequiredPoint());
            }

            MultipartFile file = update.getFile();

            String updateUrl = cloudinaryService.updateFile(file, "coupon", update.getCloudinaryId());
            coupon.setImgUrl(updateUrl);

            resultMap.put("couponId", coupon.getCouponId());
            resultMap.put("description", coupon.getDescription());
            resultMap.put("requiredPoint", coupon.getRequiredPoint());
            
            couponRepository.save(coupon);
        return resultMap;
    }


    //쿠폰 삭제
    @Transactional
    public Map<String,Object> deleteCoupon(List<String> idList) throws Exception {
        List<String> failedIds = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();

        for (String id : idList) {
            try {
                int newId = Integer.valueOf(id);
                CouponEntity coupon = couponRepository.findById(newId).orElseThrow(() -> new Exception("쿠폰 없음"));
                couponRepository.deleteById(newId);
                // Cloudinary 삭제
                cloudinaryService.deleteFile("coupon/" + coupon.getCloudinaryId());
                
            } catch (Exception e) {
                failedIds.add(id); // 삭제 실패한 id 기록
            }
        }

        if (failedIds.isEmpty()) {
            resultMap.put("resultMessage", "모든 쿠폰 삭제 완료");
            return resultMap;
        } else {
            resultMap.put("failedIds", failedIds);
            resultMap.put("resultMessage", "일부 쿠폰 삭제 실패");
            return resultMap;
        }
    }

    //쿠폰리스트 전체 가져오기(관리자)
    @Transactional(readOnly = true)
    public Map<String,Object> getCouponList(Pageable pageable) {
        Map<String,Object> resultMap = new HashMap<>();

        Page<CouponEntity> page = couponRepository.findAll(pageable);

        // 엔티티 -> DTO 변환 (파일 포함)
        List<CouponDTO> items = page
                .map(CouponEntity -> CouponDTO.of(CouponEntity))
                .getContent();

        resultMap.put("total", page.getTotalElements());
        resultMap.put("page", page.getNumber());
        resultMap.put("size", page.getSize());
        resultMap.put("content", items);
        return resultMap;
    }
     //사용자 쿠폰 요청리스트
    @Transactional(readOnly = true)
    public Map<String, Object> adminCouponList(Pageable pageable) {
        Map<String, Object> result = new HashMap<>();

         // pageable 적용 → 정렬은 Pageable에서 자동 적용
        Page<UserCouponEntity> page = userCouponRepository.findAllWithAdminCoupon(pageable);

        List<UserCouponDTO> items = page.getContent().stream().map(uc -> {
            var c = uc.getCoupon();
            return UserCouponDTO.builder()
                    .userCouponId(uc.getUserCouponId())
                    .userId(uc.getUser().getUserId())
                    .couponId(c.getCouponId())
                    .couponName(c.getCouponName())
                    .description(c.getDescription())
                    .requiredPoint(c.getRequiredPoint())
                    .imgUrl(c.getImgUrl())
                    .acquiredAt(uc.getAcquiredAt())
                    .build();
        }).toList();

        result.put("resultCode", 200);
        result.put("count", items.size());
        result.put("items", items);
        result.put("page", page.getNumber());
        result.put("size", page.getSize());
        return result;
    }

    //쿠폰교환(사용자)
    @Transactional
    public Map<String,Object> exchangeCoupon(String userId, int couponId) throws Exception {
        Map<String,Object> result = new HashMap<>();

        //쿠폰 확인
        CouponEntity coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰이 없습니다 (id=" + couponId + ")"));

        int required = coupon.getRequiredPoint();
        

        // 포인트 차감
        int updated = usersRepository.decrementPointBalanceIfEnough(userId, required);
        if (updated == 0) {
            boolean userExists = usersRepository.existsById(userId);
            if (!userExists) throw new IllegalArgumentException("사용자가 없습니다 (id=" + userId + ")");
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        //포인트 차감
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다"));

        //포인트 기록
        PointsEntity pointsEntity = PointsEntity.builder()
                .user(user)
                .sourceType(PointsEntity.SourceType.COUPON_EXCHANGE)
                .amount(-required)
                .reason("쿠폰교환: " + coupon.getCouponName())
                .build();
        pointsRepository.save(pointsEntity);

        //user_coupon 저장
        UserCouponEntity userCouponEntity = new UserCouponEntity();
        userCouponEntity.setUser(user);
        userCouponEntity.setCoupon(coupon);
        userCouponRepository.save(userCouponEntity);

        result.put("resultCode", 200);
        result.put("resultMessage", "COUPON_EXCHANGED");
        result.put("userId", userId);
        result.put("couponId", couponId);
        result.put("couponName", coupon.getCouponName());
        result.put("requiredPoint", required);
        result.put("balanceAfter", user.getPointBalance());
        result.put("acquiredAt", userCouponEntity.getAcquiredAt());

        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listMyCoupons(String userId) {
        Map<String, Object> result = new HashMap<>();

        List<UserCouponEntity> list = userCouponRepository.findAllByUserIdWithCoupon(userId);

        List<UserCouponDTO> items = list.stream().map(uc -> {
            var c = uc.getCoupon();
            return UserCouponDTO.builder()
                    .userCouponId(uc.getUserCouponId())
                    .couponId(c.getCouponId())
                    .couponName(c.getCouponName())
                    .description(c.getDescription())
                    .requiredPoint(c.getRequiredPoint())
                    .imgUrl(c.getImgUrl())
                    .acquiredAt(uc.getAcquiredAt())
                    .build();
        }).toList();

        result.put("resultCode", 200);
        result.put("count", items.size());
        result.put("items", items);
        return result;
    }
   

}
