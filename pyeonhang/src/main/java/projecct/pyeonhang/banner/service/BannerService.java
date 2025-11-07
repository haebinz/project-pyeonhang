package projecct.pyeonhang.banner.service;


import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import projecct.pyeonhang.banner.dto.BannerRequestDTO;
import projecct.pyeonhang.banner.dto.BannerResponseDTO;
import projecct.pyeonhang.banner.entity.BannerEntity;
import projecct.pyeonhang.banner.entity.BannerFileEntity;
import projecct.pyeonhang.banner.entity.QBannerEntity;
import projecct.pyeonhang.banner.repository.BannerFileRepository;
import projecct.pyeonhang.banner.repository.BannerRepository;
import projecct.pyeonhang.common.service.CloudinaryService;
import projecct.pyeonhang.common.utils.FileUtils;

import java.io.File;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerService {
    private final FileUtils fileUtils;
    private final BannerFileRepository bannerFileRepository;
    private final BannerRepository bannerRepository;
    private final CloudinaryService cloudinaryService;
    
    //파일 업로드 지정
    @Value("${server.file.upload.path}")
    private String filePath;

    private List<String> extentions =
            Arrays.asList("jpg", "jpeg", "gif", "png", "webp", "bmp", "svg");

    public List<BannerResponseDTO> getAllBanner() throws Exception{
        List<BannerEntity> bannerList = bannerRepository.findAll();
        List<BannerResponseDTO> resultList = bannerList.stream().map(bannerEntity -> BannerResponseDTO.of(bannerEntity)).toList();

        return resultList;
    }



    @Transactional
    public void saveOrUpdateBanners(List<BannerRequestDTO> requestList, List<MultipartFile> files) throws Exception{
        int fileIndex = 0;
        for(int index = 0; index < requestList.size(); index++){
            BannerRequestDTO request = requestList.get(index);
            MultipartFile file = null;

            // 파일 매핑
            if(files != null && request.getFileIndex() > -1) {
                file = files.get(request.getFileIndex());
            }

            // 배너 수정일 경우
            if(request.getBannerId() != null) {
                updateBanner(request.getBannerId(), request, file);
                // 배너 등록일 경우
            } else {
                registerBanner(request, file);
            }
        }
    }

    //배너 등록
    private void registerBanner(BannerRequestDTO request, MultipartFile file) throws Exception {
        // 파일 필수
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("파일은 필수입니다.");
        }

        Map<String, String> fileCheck = fileCheck(file);

        if (!extentions.contains(fileCheck.get("ext"))) {
            throw new RuntimeException("파일형식이 맞지 않습니다. 이미지만 가능합니다.");
        }

        String randomName = fileCheck.get("randomFileName");
        String imgUrl = cloudinaryService.uploadFile(file, "banner", randomName);

        // 배너 저장
        BannerEntity bannerEntity = new BannerEntity();
        bannerEntity.setTitle(request.getTitle());
        bannerEntity.setLinkUrl(request.getLinkUrl());
        bannerEntity.setUseYn("Y");
        bannerEntity.setImgUrl(imgUrl);
        bannerEntity.setCloudinaryId(randomName);
        bannerRepository.save(bannerEntity);

    }

    private long getFileSize(String absolutePath) {
        File f = new File(absolutePath);
        return f.exists() ? f.length() : 0L;
    }

    //배너 수정(title,linkUrl,file,useYn)
    private Map<String, Object> updateBanner(int bannerId, BannerRequestDTO update, MultipartFile file) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        try {
            BannerEntity banner = bannerRepository.findById(bannerId)
                    .orElseThrow(() -> new RuntimeException("배너를 찾을 수 없습니다."));

            // 부분 업데이트 (null이면 기존값 유지)
            if (update.getTitle() != null) {
                banner.setTitle(update.getTitle().trim());
            }
            if (update.getLinkUrl() != null) {
                banner.setLinkUrl(update.getLinkUrl().trim());
            }
            if (update.getUseYn() != null) {
                String useYn = update.getUseYn().trim().toUpperCase(Locale.ROOT);
                if (!useYn.equals("Y") && !useYn.equals("N")) {
                    throw new RuntimeException("useYn은 Y 또는 N만 허용합니다.");
                }
                banner.setUseYn(useYn);
            }

            // 파일 선택 업로드 (추가 때 쓰던 흐름 그대로)
            if (file != null && !file.isEmpty()) {

                Map<String, String> fileCheck = fileCheck(file);

                if (!extentions.contains(fileCheck.get("ext"))) {
                    throw new RuntimeException("파일형식이 맞지 않습니다. 이미지만 가능");
                }

                // 첨부 파일 있다면
                boolean isUploadDelete = cloudinaryService.deleteFile("banner/" + banner.getCloudinaryId()); // 기존 업로드한 이미지 삭제
                log.info("첨부 파일 삭제" + isUploadDelete + "아이디 : " + banner.getCloudinaryId());

                if(!isUploadDelete) {
                    throw new RuntimeException("업로드 이미지 삭제 오류");
                }

                String newImgUrl = cloudinaryService.uploadFile(file, "banner", fileCheck.get("randomFileName"));
                resultMap.put("imgUrl", newImgUrl); // 새 이미지 등록

            } else {
                // 업데이트 이미지와 기존 이미지가 없다면
                if(banner.getBannerFile() == null) {
                    throw new RuntimeException("배너 이미지가 필요합니다.");
                }
                // 기존 이미지가 있다면 기존 이미지 유지
                bannerRepository.save(banner);
            }

            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "OK");
            resultMap.put("bannerId", banner.getBannerId());
            resultMap.put("title", banner.getTitle());
            resultMap.put("linkUrl", banner.getLinkUrl());
            resultMap.put("useYn", banner.getUseYn());
            return resultMap;

        } catch (Exception e) {
            resultMap.put("resultCode", 400);
            resultMap.put("resultMessage", e.getMessage() == null ? "배너 수정 실패" : e.getMessage());
            return resultMap;
        }
    }


    //배너 삭제
    @Transactional
    public void deleteBanner(int bannerId) throws Exception {
        BannerEntity entity = bannerRepository.findById(bannerId).orElseThrow(() -> new RuntimeException("존재하지 않는 bannerId입니다."));

        bannerRepository.deleteById(bannerId);
       cloudinaryService.deleteFile("banner/" + entity.getCloudinaryId()); // 업로드 이미지 삭제
    }

    private Map<String, String> fileCheck(MultipartFile file) throws Exception {
        Map<String, String> result = new HashMap<>();
        // 확장자 체크
        String fileName = file.getOriginalFilename();
        String randomFileName = UUID.randomUUID().toString();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);

        result.put("fileName", fileName);
        result.put("randomFileName", randomFileName);
        result.put("ext", ext);

        return result;
    }

    //제품 삭제
    /*@Transactional
    public Map<String,Object> deleteCrawlingProduct(int crawlId) {
        Map<String,Object> resultMap = new HashMap<>();

        if (!crawlingRepository.existsById(crawlId)) {
            resultMap.put("resultCode", 404);
            resultMap.put("message", "crawlId를 찾을 수 없음 " + crawlId);
            return resultMap;
        }

        try {
            crawlingRepository.deleteById(crawlId); // 하드 삭제
            resultMap.put("resultCode", 200);
            resultMap.put("deletedId", crawlId);
        } catch(Exception e) {
            resultMap.put("resultCode", 500);
        }

        return resultMap;
    }*/









}
