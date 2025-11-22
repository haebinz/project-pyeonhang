package projecct.pyeonhang.common.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudService {
    String uploadFile(MultipartFile file, String folder, String publicId) throws Exception;
    String uploadFile(MultipartFile file, String folder, String publicId, int width, int height) throws Exception;
    boolean deleteFile(String publicId) throws Exception;
}
