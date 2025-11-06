package projecct.pyeonhang.banner.service;


import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import projecct.pyeonhang.banner.dto.BannerRequestDTO;
import projecct.pyeonhang.banner.dto.BannerResponseDTO;
import projecct.pyeonhang.banner.entity.BannerEntity;
import projecct.pyeonhang.banner.entity.BannerFileEntity;
import projecct.pyeonhang.banner.repository.BannerFileRepository;
import projecct.pyeonhang.banner.repository.BannerRepository;
import projecct.pyeonhang.common.utils.FileUtils;

import java.io.File;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BannerService {
    private final FileUtils fileUtils;
    private final BannerFileRepository bannerFileRepository;
    private final BannerRepository bannerRepository;
    
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
    //배너 등록
    @Transactional
    public void registerBanner(BannerRequestDTO request) throws Exception {
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

        // 배너 저장
        BannerEntity bannerEntity = new BannerEntity();
        bannerEntity.setTitle(request.getTitle());
        bannerEntity.setLinkUrl(request.getLinkUrl());
        bannerEntity.setUseYn("Y");
        bannerRepository.save(bannerEntity);

        // 파일 저장
        BannerFileEntity bannerFileEntity = new BannerFileEntity();
        bannerFileEntity.setBanner(bannerEntity);
        bannerFileEntity.setFileName(fileMap.get("fileName").toString());
        bannerFileEntity.setStoredName(fileMap.get("storedFileName").toString());
        bannerFileEntity.setFilePath(filePath);
        bannerFileEntity.setFileSize(getFileSize(filePath + bannerFileEntity.getStoredName()));
        bannerFileRepository.save(bannerFileEntity);
    }

    private long getFileSize(String absolutePath) {
        File f = new File(absolutePath);
        return f.exists() ? f.length() : 0L;
    }

    //배너 수정(title,linkUrl,file,useYn)
    @Transactional
    public Map<String, Object> updateBanner(int bannerId, BannerRequestDTO update) throws Exception {
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
            MultipartFile file = update.getFile();
            if (file != null && !file.isEmpty()) {
                String fileName = file.getOriginalFilename();
                String ext = fileName.substring(fileName.lastIndexOf(".") + 1)
                        .toLowerCase(Locale.ROOT);
                if (!extentions.contains(ext)) {
                    throw new RuntimeException("파일형식이 맞지 않습니다. 이미지만 가능");
                }

                // 업로드 실행
                Map<String, Object> fileMap = fileUtils.uploadFiles(file,filePath);
                if (fileMap == null) throw new RuntimeException("파일 업로드가 실패했습니다.");

                // 파일 메타 저장
                BannerFileEntity fileEntity = new BannerFileEntity();
                fileEntity.setBanner(banner);
                fileEntity.setFileName(fileMap.get("fileName").toString());
                fileEntity.setStoredName(fileMap.get("storedFileName").toString());
                fileEntity.setFilePath(filePath);
                fileEntity.setFileSize(getFileSize(filePath + fileEntity.getStoredName()));
                bannerFileRepository.save(fileEntity);

                // 응답용 최신 파일 정보
                resultMap.put("fileName", fileEntity.getFileName());
                resultMap.put("storedName", fileEntity.getStoredName());
                resultMap.put("filePath", fileEntity.getFilePath());
                resultMap.put("fileSize", fileEntity.getFileSize());
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
    public Map<String,Object> deleteBanner(int bannerId) throws Exception {
        Map<String,Object> resultMap = new HashMap<>();
       if(!bannerRepository.existsById(bannerId)){
           resultMap.put("resultCode",404);
           resultMap.put("message","존재하지 않는 bannerId입니다"+bannerId);
           return resultMap;
       }
       try{
           bannerRepository.deleteById(bannerId);
           resultMap.put("resultCode",200);
           resultMap.put("삭제된 bannerId",bannerId);
       } catch (Exception e) {
           resultMap.put("resultCode",500);
           resultMap.put("message",e.getMessage());
           resultMap.put("삭제완료되었씁니다", HttpStatus.OK);
       }
        return resultMap;
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
