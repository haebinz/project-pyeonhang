package projecct.pyeonhang.common.service;

import com.cloudinary.Cloudinary;

import com.cloudinary.utils.ObjectUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryService implements CloudService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /*
        file: 파일,
        folder: 저장할 폴더 이름
        publicId : 저장할 파일 이름
     */
    @Override
    public String uploadFile(MultipartFile file, String folder, String publicId) throws Exception {

        validateFile(file);

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,
                "public_id", publicId
        ));
        return uploadResult.get("secure_url").toString();
    }

    @Override
    /* publicId : 업로드 파일 경로를 포함한 cloudinaryId ex) coupon/dfs02318dl */
    public boolean deleteFile(String publicId) throws Exception {
        log.info("삭제 요청 파일 : " +publicId);
        Map deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        return "ok".equals(deleteResult.get("result"));
    }


    public String updateFile(MultipartFile file, String folder, String publicId) throws Exception  {

        validateFile(file);

            log.info("파일 이름: {}", file.getOriginalFilename());
        try {
            Map updateResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", publicId,
                    "overwrite", true,
                    "invalidate", true
                )
            );
            log.info("이미지 업데이트 완료: {}", updateResult);
            return (String) updateResult.get("url");
        } catch(IOException e) {
            log.error("이미지 업데이트 실패", e);
            throw new Exception("Cloudinary 업로드 실패", e);
        }
    }

    
    // 파일 형식 검사
    private List<String> extentions =
            Arrays.asList("jpg", "jpeg", "gif", "png", "webp", "bmp", "svg");    

    private void validateFile(MultipartFile file) throws Exception {
        Map<String, String> fileCheck = fileCheck(file);
        if (!extentions.contains(fileCheck.get("ext"))) {
            throw new RuntimeException("파일형식이 맞지 않습니다. 이미지만 가능합니다.");
        }
    }    

    private Map<String, String> fileCheck(MultipartFile file) throws Exception {
        Map<String, String> result = new HashMap<>();
        String fileName = file.getOriginalFilename();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        result.put("fileName", fileName);
        result.put("ext", ext);
        return result;
    }    
}
