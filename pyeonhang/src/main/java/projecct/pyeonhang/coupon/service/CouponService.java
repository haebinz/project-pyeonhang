package projecct.pyeonhang.coupon.service;

import lombok.RequiredArgsConstructor;
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


import java.io.File;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final FileUtils fileUtils;
    private final CouponRepository couponRepository;
    private final CouponFileRepository couponFileRepository;

    //파일 업로드 지정
    @Value("${server.file.coupon.path}")
    private String filePath;

    private List<String> extentions =
            Arrays.asList("jpg", "jpeg", "gif", "png", "webp", "bmp");

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

        try {
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
                String ext = fileName.substring(fileName.lastIndexOf(".") + 1)
                        .toLowerCase(Locale.ROOT);
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

                fileEntity.setFileName(fileMap.get("fileName").toString());
                fileEntity.setStoredName(fileMap.get("storedFileName").toString());
                fileEntity.setFilePath(fileMap.get("filePath").toString());
                fileEntity.setFileSize(getFileSize(filePath + fileEntity.getStoredName()));
                couponFileRepository.save(fileEntity);

                resultMap.put("fileName", fileEntity.getFileName());
                resultMap.put("storedName", fileEntity.getStoredName());
                resultMap.put("filePath", fileEntity.getFilePath());
                resultMap.put("fileSize", fileEntity.getFileSize());
            }

            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
            resultMap.put("couponId", coupon.getCouponId());
            resultMap.put("description", coupon.getDescription());
            resultMap.put("requiredPoint", coupon.getRequiredPoint());
        } catch (Exception e) {
            resultMap.put("resultCode", 500);
            resultMap.put("resultMessage", e.getMessage());
            e.printStackTrace();
        }
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


}
