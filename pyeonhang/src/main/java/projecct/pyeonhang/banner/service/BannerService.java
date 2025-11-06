package projecct.pyeonhang.banner.service;


import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import projecct.pyeonhang.banner.dto.BannerDTO;
import projecct.pyeonhang.banner.dto.BannerRequestDTO;
import projecct.pyeonhang.banner.dto.BannerUpdateDTO;
import projecct.pyeonhang.banner.entity.BannerEntity;
import projecct.pyeonhang.banner.entity.BannerFileEntity;
import projecct.pyeonhang.banner.repository.BannerFileRepository;
import projecct.pyeonhang.banner.repository.BannerRepository;
import projecct.pyeonhang.common.dto.PageVO;
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
            Arrays.asList("jpg", "jpeg", "gif", "png", "webp", "bmp");

    @Transactional(readOnly = true)
    public Map<String, Object> getBannerList(Pageable pageable) {
        Map<String, Object> resultMap = new HashMap<>();

        Page<BannerEntity> pageList = bannerRepository.findAll(pageable); // @EntityGraph(file) 적용됨

        PageVO pageVO = new PageVO();
        pageVO.setData(pageList.getNumber(), (int) pageList.getTotalElements());

        List<BannerDTO> contents = pageList.getContent().stream()
                .map(BannerDTO::of)
                .toList();

        resultMap.put("total", pageList.getTotalElements());
        resultMap.put("page", pageList.getNumber());
        resultMap.put("banners", contents);
        // resultMap.put("pageInfo", pageVO);
        return resultMap;
    }

    //배너 등록
    @Transactional
    public void registerBanner(BannerRequestDTO request) throws Exception {
        MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) throw new RuntimeException("파일은 필수입니다.");

        String fileName = file.getOriginalFilename();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        if (!extentions.contains(ext)) throw new RuntimeException("파일형식이 맞지 않습니다. 이미지만 가능합니다.");

        Map<String, Object> fileMap = fileUtils.uploadFiles(file, filePath);
        if (fileMap == null) throw new RuntimeException("파일 업로드가 실패했습니다.");

        BannerEntity banner = new BannerEntity();
        banner.setTitle(request.getTitle());
        banner.setLinkUrl(request.getLinkUrl());
        banner.setUseYn("Y");

        BannerFileEntity fileEntity = new BannerFileEntity();
        fileEntity.setFileName(fileMap.get("fileName").toString());
        fileEntity.setStoredName(fileMap.get("storedFileName").toString());
        fileEntity.setFilePath(filePath);
        fileEntity.setFileSize(getFileSize(filePath + fileEntity.getStoredName()));


        banner.setFile(fileEntity);
        bannerRepository.save(banner);
    }

    private long getFileSize(String absolutePath) {
        File f = new File(absolutePath);
        return f.exists() ? f.length() : 0L;
    }

    //배너 수정(title,linkUrl,file,useYn)
    @Transactional
    public Map<String, Object> updateBanner(int bannerId, BannerUpdateDTO update) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        try {
            BannerEntity banner = bannerRepository.findById(bannerId)
                    .orElseThrow(() -> new RuntimeException("배너를 찾을 수 없습니다."));

            if (update.getTitle() != null) banner.setTitle(update.getTitle().trim());
            if (update.getLinkUrl() != null) banner.setLinkUrl(update.getLinkUrl().trim());
            if (update.getUseYn() != null) {
                String useYn = update.getUseYn().trim().toUpperCase(Locale.ROOT);
                if (!useYn.equals("Y") && !useYn.equals("N")) throw new RuntimeException("useYn은 Y 또는 N만 허용");
                banner.setUseYn(useYn);
            }

            MultipartFile file = update.getFile();
            if (file != null && !file.isEmpty()) {
                String fileName = file.getOriginalFilename();
                String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
                if (!extentions.contains(ext)) throw new RuntimeException("파일형식이 맞지 않습니다. 이미지만 가능");

                Map<String, Object> fileMap = fileUtils.uploadFiles(file, filePath);
                if (fileMap == null) throw new RuntimeException("파일 업로드가 실패했습니다.");

                BannerFileEntity fe = banner.getFile();
                if (fe == null) {
                    fe = new BannerFileEntity();
                    banner.setFile(fe); // 양방향 세팅
                }
                fe.setFileName(fileMap.get("fileName").toString());
                fe.setStoredName(fileMap.get("storedFileName").toString());
                fe.setFilePath(filePath);
                fe.setFileSize(getFileSize(filePath + fe.getStoredName()));

                resultMap.put("fileName", fe.getFileName());
                resultMap.put("storedName", fe.getStoredName());
                resultMap.put("filePath", fe.getFilePath());
                resultMap.put("fileSize", fe.getFileSize());
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

}
