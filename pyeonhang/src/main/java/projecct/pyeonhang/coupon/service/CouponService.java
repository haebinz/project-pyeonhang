package projecct.pyeonhang.coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import projecct.pyeonhang.common.dto.PageVO;
import projecct.pyeonhang.common.utils.FileUtils;
import projecct.pyeonhang.coupon.dto.CouponDTO;
import projecct.pyeonhang.coupon.dto.CouponRequestDTO;
import projecct.pyeonhang.coupon.dto.CouponUpdateDTO;
import projecct.pyeonhang.coupon.entity.CouponEntity;
import projecct.pyeonhang.coupon.entity.CouponFileEntity;
import projecct.pyeonhang.coupon.repository.CouponFileRepository;
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
    private final FileUtils fileUtils;
    private final CouponRepository couponRepository;
    private final CouponFileRepository couponFileRepository;
    private final UsersRepository usersRepository;
    private final PointsRepository pointsRepository;
    private final UserCouponRepository userCouponRepository;

    //파일 업로드 지정
    @Value("${server.file.coupon.path}")
    private String filePath;

    private List<String> extentions =
            Arrays.asList("jpg", "jpeg", "gif", "png", "webp", "bmp", "svg");

    @Transactional
    public void registerCoupon(CouponRequestDTO request) throws Exception {
        // 파일 필수
        MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("파일은 필수입니다.");
        }
        // 확장자 체크
        String fileName = file.getOriginalFilename();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        if (!extentions.contains(ext)) {
            throw new RuntimeException("파일형식이 맞지 않습니다. 이미지만 가능합니다.");
        }



        // 업로드
        Map<String, Object> fileMap = fileUtils.uploadFiles(file, filePath);
        if (fileMap == null) throw new RuntimeException("파일 업로드가 실패했습니다.");

        //쿠폰 저장
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName(request.getCouponName());
        couponEntity.setDescription(request.getDescription());
        couponEntity.setRequiredPoint(request.getRequiredPoint());
        couponRepository.save(couponEntity);

        //파일 저장
        CouponFileEntity couponFileEntity = new CouponFileEntity();
        couponFileEntity.setCoupon(couponEntity);
        couponFileEntity.setFileName(fileMap.get("fileName").toString());
        couponFileEntity.setStoredName(fileMap.get("storedFileName").toString());
        couponFileEntity.setFilePath(filePath);
        couponFileEntity.setFileSize(getFileSize(filePath + couponFileEntity.getStoredName()));
        couponFileRepository.save(couponFileEntity);
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
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.contains(".")) {
                throw new RuntimeException("파일 이름 또는 확장자가 없습니다.");
            }

            String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
            if (!extentions.contains(ext)) {
                throw new RuntimeException("파일형식이 맞지 않습니다. 이미지만 가능");
            }


            Map<String, Object> fileMap = fileUtils.uploadFiles(file, filePath);
            if (fileMap == null) throw new RuntimeException("파일 업로드가 실패했습니다.");


            CouponFileEntity fileEntity = coupon.getFile();
            if (fileEntity == null) {
                fileEntity = new CouponFileEntity();
                fileEntity.setCoupon(coupon);
                coupon.setFile(fileEntity);
            }


            String storedName = fileMap.get("storedFileName").toString();
            String savedFileName = fileMap.get("fileName").toString();
            String savedFilePath = fileMap.get("filePath").toString();

            fileEntity.setFileName(savedFileName);
            fileEntity.setStoredName(storedName);
            fileEntity.setFilePath(savedFilePath);
            fileEntity.setFileSize(getFileSize(savedFilePath + storedName));


            couponFileRepository.save(fileEntity);


            resultMap.put("fileName", fileEntity.getFileName());
            resultMap.put("storedName", fileEntity.getStoredName());
            resultMap.put("filePath", fileEntity.getFilePath());
            resultMap.put("fileSize", fileEntity.getFileSize());
        }

        couponRepository.save(coupon);

      
        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "OK");
        resultMap.put("couponId", coupon.getCouponId());
        resultMap.put("couponName", coupon.getCouponName());
        resultMap.put("description", coupon.getDescription());
        resultMap.put("requiredPoint", coupon.getRequiredPoint());

        return resultMap;
    }



    //쿠폰 삭제
    @Transactional
    public Map<String,Object> deleteCoupon(int couponId) throws Exception {
        Map<String,Object> resultMap = new HashMap<>();
        try{
            couponRepository.findById(couponId);
            if(!couponRepository.findById(couponId).isPresent()){
                resultMap.put("resultCode", 500);
                resultMap.put("message","존재하지 않는 쿠폰 id입니다");
            }
            couponRepository.deleteById(couponId);
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
        }catch (Exception e){
            resultMap.put("resultCode", 500);
            resultMap.put("resultMessage", e.getMessage());
        }
        return resultMap;
    }

    //쿠폰리스트 전체 가져오기(관리자)
    @Transactional(readOnly = true)
    public Map<String,Object> getCouponList(Pageable pageable) {
        Map<String,Object> resultMap = new HashMap<>();

        Page<CouponEntity> page = couponRepository.findAll(pageable);

        // 엔티티 -> DTO 변환 (파일 포함)
        List<CouponDTO> items = page
                .map(CouponEntity -> CouponDTO.of(CouponEntity, CouponEntity.getFile()))
                .getContent();

        resultMap.put("total", page.getTotalElements());
        resultMap.put("page", page.getNumber());
        resultMap.put("size", page.getSize());
        resultMap.put("content", items);
        return resultMap;
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

        List<UserCouponEntity> list = userCouponRepository.findAllByUserIdWithCouponAndFile(userId);

        List<UserCouponDTO> items = list.stream().map(uc -> {
            var c = uc.getCoupon();
            var f = c.getFile();
            return UserCouponDTO.builder()
                    .userCouponId(uc.getUserCouponId())
                    .couponId(c.getCouponId())
                    .couponName(c.getCouponName())
                    .description(c.getDescription())
                    .requiredPoint(c.getRequiredPoint())
                    .fileName(f != null ? f.getFileName() : null)
                    .storedName(f != null ? f.getStoredName() : null)
                    .filePath(f != null ? f.getFilePath() : null)
                    .acquiredAt(uc.getAcquiredAt())
                    .build();
        }).toList();

        result.put("resultCode", 200);
        result.put("count", items.size());
        result.put("items", items);
        return result;
    }

}
