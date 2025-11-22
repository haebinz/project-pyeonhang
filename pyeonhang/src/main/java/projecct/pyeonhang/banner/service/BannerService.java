package projecct.pyeonhang.banner.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import projecct.pyeonhang.banner.dto.BannerRequestDTO;
import projecct.pyeonhang.banner.dto.BannerResponseDTO;
import projecct.pyeonhang.banner.entity.BannerEntity;
import projecct.pyeonhang.banner.repository.BannerRepository;
import projecct.pyeonhang.common.service.CloudinaryService;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerService {
    private final BannerRepository bannerRepository;
    private final CloudinaryService cloudinaryService;


    // 모든 배너 출력
    @Transactional(readOnly = true)
    public List<BannerResponseDTO> getAllBanner() {
        return bannerRepository.findAllByOrderByBannerOrderAsc()
                .stream()
                .map(BannerResponseDTO::of)
                .toList();
    }

    // 노출여부 "Y"인 배너 출력
    @Transactional(readOnly = true)
    public List<BannerResponseDTO> getUseYBanner() {
        return bannerRepository.findByUseYnOrderByBannerOrderAsc("Y")
                .stream()
                .map(BannerResponseDTO::of)
                .toList();
    }

    @Transactional
    public void saveOrUpdateBanners(List<BannerRequestDTO> requestList, List<MultipartFile> files) throws Exception {
        

        // 1. 활성화된 배너 개수 확인
        long activeCount = bannerRepository.countByUseYn("Y");
        
        // 2. 새로 등록되는 활성 배너 개수
        long newActiveBannerCount = requestList.stream()
                .filter(req -> req.getBannerId() == null)
                .filter(req -> "Y".equals(req.getUseYn()) || req.getUseYn() == null) // useYn이 null이면 기본값 "Y"
                .count();
        
        // 3. 최대 5개 제한
        if (activeCount + newActiveBannerCount > 5) {
            throw new RuntimeException(
                String.format("활성 배너는 최대 5개까지만 등록 가능합니다.", 
                            activeCount, newActiveBannerCount)
            );
        }


        // 1. 병렬 업로드 (파일이 있는 것만)
        // CompletableFuture 비동기 작업을 하기 위한 클래스
        Map<Integer, CompletableFuture<String>> uploadFutures = new HashMap<>();
        
        for (int index = 0; index < requestList.size(); index++) {
            BannerRequestDTO request = requestList.get(index);
            // 파일 매핑
            MultipartFile file = getFile(files, request.getFileIndex());

            log.info("요청 배너 아이디 :" + request.getBannerId());

            if (file != null && !file.isEmpty()) {
                String bannerId = request.getBannerId() != null ? 
                    request.getBannerId() : UUID.randomUUID().toString();
                
                final int idx = index;
                final MultipartFile currentFile = file;
                final String currentBannerId = bannerId;
                
                // CompletableFuture.supplyAsync 결과값을 반환하는 비동기 작업 실행
                // uploadFutures 객체에 supplyAsync 작업 진행 중인 객체 저장
                uploadFutures.put(idx, CompletableFuture.supplyAsync(() -> {
                    try {
                        // 이미지 변경일 때
                        if (request.getBannerId() != null) {
                            return cloudinaryService.updateFile(file, "banner", currentBannerId);
                        }
                        request.setBannerId(currentBannerId);
                        return cloudinaryService.uploadFile(currentFile, "banner", currentBannerId);
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }));
            }
        }
        
        // 2. 업로드 완료 대기
        // CompletableFuture.allOf(CompletableFuture.supplyAsync, CompletableFuture.supplyAsync...) 
        // 인자로 받은 비동기 작업이 모두 끝나면 실행
        CompletableFuture.allOf(uploadFutures.values().toArray(new CompletableFuture[0])).join();
        
        // 3. DB 저장
        for (int index = 0; index < requestList.size(); index++) {
            BannerRequestDTO request = requestList.get(index);
            Optional<BannerEntity> bannerOpt = bannerRepository.findById(request.getBannerId());

            BannerEntity banner;

            // 배너 엔터티가 있으면 배너 변수에 담기
            if(bannerOpt.isPresent()) {
                banner = bannerOpt.get();
            } else {
                // 배너 엔터티가 없으면 새 배너 엔터티 만들기
                banner = new BannerEntity();
                banner.setBannerId(request.getBannerId());
                banner.setUseYn("Y");
                // ID는 DB에서 자동 생성되도록 놔두는 게 안전
            }


            
            // 필드 업데이트
            updateBannerFields(banner, request, index);
            
            // 업로드된 이미지 URL 설정
            if (uploadFutures.containsKey(index)) {
                banner.setImgUrl(uploadFutures.get(index).join());
            }
            
            bannerRepository.save(banner);
        }
    }

    @Transactional
    public void deleteBanner(String bannerId) throws Exception {
        bannerRepository.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 bannerId입니다."));

        boolean isDelete = cloudinaryService.deleteFile("banner/" + bannerId);
        // cloudinary에서 파일이 삭제됐을 경우
        if(isDelete) {
            bannerRepository.deleteById(bannerId);
        } else {
            throw new Exception("cloudinary 업로드 파일 삭제 실패");
        }
    }

    private MultipartFile getFile(List<MultipartFile> files, Integer fileIndex) {
        if (files == null || fileIndex == null || fileIndex < 0 || fileIndex >= files.size()) {
            return null;
        }
        return files.get(fileIndex);
    }

    private void updateBannerFields(BannerEntity banner, BannerRequestDTO request, int index) {
        if (request.getTitle() != null) banner.setTitle(request.getTitle());
        if (request.getLinkUrl() != null) banner.setLinkUrl(request.getLinkUrl());
        if (request.getUseYn() != null) banner.setUseYn(request.getUseYn());
        banner.setBannerOrder(index);
    }
}
