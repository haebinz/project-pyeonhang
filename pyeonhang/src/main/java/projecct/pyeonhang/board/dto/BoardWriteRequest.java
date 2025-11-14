package projecct.pyeonhang.board.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class BoardWriteRequest {

    @NotBlank(message="글 제목을 입력해주세요")
    private String title;

    @NotBlank(message="내용을 입력해주세요")
    private String contents;


}
