package projecct.pyeonhang.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
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
    public String uploadFile(MultipartFile file, String folder, String publicId, int width, int height) throws Exception {

        validateFile(file);

        cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,
                "public_id", publicId
        ));

    // 리사이즈 URL 생성
        String resizedUrl = cloudinary.url()
                .transformation(new Transformation()
                        .width(width)
                        .height(height)
                        .crop("fill")
                        .gravity("auto")
                        .quality("auto")
                        .fetchFormat("auto")
                )
                .generate(folder + "/" + publicId);

        return resizedUrl;
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


    // 파일 폴더 삭제
    public void deleteBoardFolder(int brdId) throws Exception {
        String folderPath = "board/" + brdId;

        try {
            // 1. 폴더 안의 모든 리소스 삭제
            // 폴더 아래의 모든 파일 검색
            Map result = cloudinary.api().resources(
                    ObjectUtils.asMap(
                            "type", "upload",
                            "prefix", folderPath + "/"   // 중요
                    )
            );

            List<Map> resources = (List<Map>) result.get("resources");

            for (Map res : resources) {
                String publicId = (String) res.get("public_id");

                cloudinary.api().deleteResources(
                        List.of(publicId),
                        ObjectUtils.emptyMap()
                );
            }

            // 2. 폴더 삭제
            cloudinary.api().deleteFolder(folderPath, ObjectUtils.emptyMap());

            System.out.println("Cloudinary folder deleted: " + folderPath);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete folder: " + folderPath, e);
        }
    }

    
    // 파일 형식 검사
    private List<String> extentions =
            Arrays.asList("jpg", "jpeg", "gif", "png", "webp", "bmp");    

    private void validateFile(MultipartFile file) throws Exception {
        Map<String, String> fileCheck = fileCheck(file);
        if (!extentions.contains(fileCheck.get("ext"))) {
            throw new RuntimeException(fileCheck.get("ext")+ "파일은 업로드가 불가능합니다.");
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
