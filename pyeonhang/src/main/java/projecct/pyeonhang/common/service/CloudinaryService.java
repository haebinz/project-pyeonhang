package projecct.pyeonhang.common.service;

import com.cloudinary.Cloudinary;

import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class CloudinaryService implements CloudService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadFile(MultipartFile file, String folder, String publicId) throws Exception {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,
                "public_id", publicId,
                "overwrite", true
        ));
        return uploadResult.get("secure_url").toString();
    }

    @Override
    public boolean deleteFile(String publicId) throws Exception {
        Map deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        return "ok".equals(deleteResult.get("result"));
    }

}
