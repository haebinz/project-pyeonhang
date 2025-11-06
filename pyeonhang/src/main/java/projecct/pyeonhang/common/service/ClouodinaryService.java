package projecct.pyeonhang.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

//@Service
//@RequiredArgsConstructor
//public class ClouodinaryService implements CloudService {
//    private final CloudService  cloudService;
//
//    @Override
//    public String uploadFile(MultipartFile file, String folder, String publicId) throws Exception {
//        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
//                "folder", folder,
//                "public_id", publicId,
//                "overwrite", true
//        ));
//        return uploadResult.get("secure_url").toString();
//    }
//
//    @Override
//    public boolean deleteFile(String publicId) throws Exception {
//        Map deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
//        return "ok".equals(deleteResult.get("result"));
//    }
//
//}
