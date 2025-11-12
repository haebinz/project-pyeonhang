package projecct.pyeonhang.email.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CodeRequest {
    @NotBlank(message = "인증 코드를 입력하세요")
    private String code;
}
