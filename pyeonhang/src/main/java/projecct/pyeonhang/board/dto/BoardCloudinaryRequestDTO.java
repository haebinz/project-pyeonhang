package projecct.pyeonhang.board.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardCloudinaryRequestDTO {

    private String imageUrl;
    private String cloudinaryId;
    private List<MultipartFile> files;




}
