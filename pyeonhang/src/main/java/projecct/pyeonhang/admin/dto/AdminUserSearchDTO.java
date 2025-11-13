package projecct.pyeonhang.admin.dto;

import lombok.Data;

@Data
//관리자페이지->사용자 검색
public class AdminUserSearchDTO {
    //검색할단어
    private String searchText;
    private String roleFilter;
    private String delYn;
}
