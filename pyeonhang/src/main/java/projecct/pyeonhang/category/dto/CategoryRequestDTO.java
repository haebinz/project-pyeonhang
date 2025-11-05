package projecct.pyeonhang.category.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CategoryRequestDTO {

    private String categoryName;

    private String categoryCode;

    private String useYn;


}
